import java.util.*;

/**
 * Version 0 : Sorted by startTask, stopped because it was dirty and too complex
 * Version 1 : Started with a n²/2 time complexity solution, giving a heuristic of the result
 * (start with the tasks taking less time and see if they conflicts with the one already chosen)
 * Issues :
 * - O(n²) is too much
 * - The result needed to be optimal
 *
 * Version 2 : After research, an algorithm give an optimal n.log n solution for this problem.
 * The algorithm used is Greedy-Iterative-Activity-Selector
 * - Start sorting every task by endTime (ascending) => O(n.log n)
 * - Run the algorithm => O(n)
 *      - Take the first task (let's call it lastTaskDone).
 *      - Go through the next task and see if it conflicts with the last one.
 *          - If it doesn't, change lastTaskDone to that task and add +1 to the number of tasks done
 *          - Otherwise, continue to the next task
 *
 * This is optimal because :
 * - If another task conflicts with that one, well it's one for one, and because it ends before, the first to end will
 * always be better in terms of number of conflicts.
 * - If more than one task conflicts with that one, because they conflict with the first to end compared with them, you
 * wouldn't have been able to fit more than one in any case.
 * - This works for every task
 *
 * More info @ https://en.wikipedia.org/wiki/Activity_selection_problem
 */
class Solution {

    public static void main(String args[]) {

        List<ScientificWork> scientificWorks = Solution.parseInput();
        scientificWorks.sort(Comparator.comparing(ScientificWork::getEndWork));
        System.out.println(Solution.runAlgorithm(scientificWorks));
    }

    public static List<ScientificWork> parseInput() {

        Scanner in = new Scanner(System.in);

        int N = in.nextInt();
        Map<Integer, Integer> startDayDurationMap = new HashMap<>();
        for (int i = 0; i < N; i++) {
            int J = in.nextInt();
            int D = in.nextInt();
            if(!startDayDurationMap.containsKey(J) || startDayDurationMap.get(J) > D) {
                startDayDurationMap.put(J, D);
            }
        }
        Set<Integer> works = startDayDurationMap.keySet();
        List<ScientificWork> scientificWorks = new ArrayList<>();
        for(Integer work: works) {
            scientificWorks.add(new ScientificWork(work, startDayDurationMap.get(work)));
        }
        return scientificWorks;
    }

    public static int runAlgorithm(List<ScientificWork> scientificWorks) {
        int nbCalculus = 1;
        ScientificWork lastSciWork = scientificWorks.get(0);
        for(ScientificWork scientificWork : scientificWorks) {
            if(!scientificWork.conflictWith(lastSciWork)) {
                lastSciWork = scientificWork;
                nbCalculus++;
            }
        }
        return nbCalculus;
    }
}


class ScientificWork {

    private Integer startWork;
    private Integer duration;
    private Integer endWork;

    public ScientificWork(Integer startWork, Integer duration) {
        this.startWork = startWork;
        this.duration = duration;
        this.endWork = this.startWork + this.duration - 1;
    }

    public boolean conflictWith(ScientificWork sw) {
        return this.endWork >= sw.startWork && this.startWork <= sw.endWork;
    }

    public Integer getStartWork() {
        return startWork;
    }

    public Integer getDuration() {
        return duration;
    }

    public Integer getEndWork() {
        return endWork;
    }

    @Override
    public String toString() {
        return "Starting on day " + startWork + " and ending on day " + endWork + " (for " + duration + " days)";
    }
}
