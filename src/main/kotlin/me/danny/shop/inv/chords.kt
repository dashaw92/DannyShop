package me.danny.shop.inv

import org.bukkit.event.inventory.*
import java.util.function.*

//Built with ChordBuilder for convenience
//Represents a "chorded" action in a menu that keeps state between clicks
internal class Chord<T>(
    //The actions to take at each step
    //Actions increment by one when the previous action
    //returns StateResult.Accepted
    //The step counter resets when the previous action
    //returns StateResult.ResetSteps
    private val recipe: Map<Int, ChordAction<T>>,
    //Supplier of initial state. Used when the
    //state is ready (we're done with the last step)
    //or the previous action provides a ResetSteps result
    private val stateBuilder: Supplier<T>,
    //When the chord is complete, this is invoked with the
    //final output state
    private val terminal: (T) -> Unit
) {

    //Total number of steps this action contains
    private val steps: Int = recipe.size

    // Internal state: Where we are in the action
    private var current = 0

    // Potentially half-baked state object with intermediate data
    private var state = stateBuilder.get()

    // Run the next action in the recipe
    // May not progress the current step if the
    // action returns false for this event
    // If the Chord is already ready to produce the final
    // output, this does nothing
    fun next(event: InventoryClickEvent) {
        when (recipe[current]!!.test(event, state)) {
            StateResult.ResetSteps -> {
                getAndReset()
                return
            }

            StateResult.Accepted -> current += 1
            StateResult.Rejected -> {
                return
            }
        }

        //Check if the recipe is completed, and if so,
        //call the terminal action with the final state
        //and then reset the internal state to be ready for
        //the next iteration of actions
        if (isReady()) {
            terminal(getAndReset())
        }
    }

    //Check if the action has enough input to produce the output
    private fun isReady(): Boolean = current == steps

    //Get the output value, ready or not,
    //and then reset internal state with the
    //stateBuilder
    private fun getAndReset(): T {
        current = 0
        val output = state
        state = stateBuilder.get()
        return output
    }
}

//Public API for building Chords
internal class ChordBuilder<T>(private val stateBuilder: Supplier<T>) {
    private val recipe: MutableMap<Int, ChordAction<T>> = mutableMapOf()
    private var terminal: ((T) -> Unit)? = null

    //Add an action that should be repeated `amount` times
    fun loopStep(amount: Int, action: ChordAction<T>): ChordBuilder<T> {
        for (i in 0..<amount) {
            val next = recipe.size
            recipe[next] = action
        }
        return this
    }

    //Sequentially add actions
    fun withStep(action: ChordAction<T>): ChordBuilder<T> {
        val next = recipe.size
        recipe[next] = action
        return this
    }

    //Run when the recipe is completed,
    //a lambda that accepts the final state
    fun withTerminal(action: (T) -> Unit): ChordBuilder<T> {
        this.terminal = action
        return this
    }

    //Build it!
    fun build(): Chord<T> {
        return Chord(recipe, stateBuilder, terminal ?: { })
    }
}

//Lambda compatible
//When it returns:
// * Accepted: The action was successful, and we should go to the next step
// * Rejected: The action was NOT successful, keep trying this step
// * ResetSteps: Action was NOT successful, and we should start over from step 1
internal fun interface ChordAction<T> {
    fun test(event: InventoryClickEvent, state: T): StateResult
}

internal enum class StateResult {
    Accepted,
    Rejected,
    ResetSteps
}