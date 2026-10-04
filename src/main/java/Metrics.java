/**
 * Operation counters. Every structure increments them INSIDE its methods.
 *  steps       - one read of an array cell OR one move to the next node
 *  moves       - one element shifted/written in an array OR one link (pointer) update in a list
 *  comparisons - one comparison of two elements
 */
public class Metrics {
    public long steps = 0;
    public long moves = 0;
    public long comparisons = 0;

    public void reset() {
        steps = 0;
        moves = 0;
        comparisons = 0;
    }
}
