package edu.scheduler;

import java.util.*;

import org.springframework.stereotype.Service;

import static edu.scheduler.SchedulerController.*;

@Service
public class SchedulingService {
    public SimulationResponse simulate(SimulationRequest request) {
        if (request == null || request.processes() == null || request.processes().isEmpty())
            throw new IllegalArgumentException("Add at least one process.");
        String algorithm = request.algorithm() == null ? "" : request.algorithm().toUpperCase(Locale.ROOT);
        if (!algorithm.equals("ROUND_ROBIN") && !algorithm.equals("PRIORITY"))
            throw new IllegalArgumentException("Algorithm must be ROUND_ROBIN or PRIORITY.");
        if (algorithm.equals("ROUND_ROBIN") && (request.quantum() == null || request.quantum() < 1))
            throw new IllegalArgumentException("Round Robin requires a positive time quantum.");
        LinkedList<Job> jobs = new LinkedList<>();
        Set<Integer> ids = new HashSet<>();
        for (ProcessInput p : request.processes()) {
            if (p == null || p.arrivalTime() < 0 || p.burstTime() < 1)
                throw new IllegalArgumentException("Arrival times must be non-negative and burst times positive.");
            if (!ids.add(p.processId())) throw new IllegalArgumentException("Process IDs must be unique.");
            jobs.add(new Job(p));
        }
        jobs.sort(Comparator.comparingInt(j -> j.input.arrivalTime()));
        List<Segment> timeline = new ArrayList<>();
        int total = algorithm.equals("ROUND_ROBIN") ? roundRobin(jobs, request.quantum(), timeline) : priority(jobs, timeline);
        Map<Integer, Job> byId = new HashMap<>();
        for (Job j : jobs) byId.put(j.input.processId(), j);
        List<ProcessResult> results = new ArrayList<>();
        double wait = 0, turn = 0;
        for (ProcessInput p : request.processes()) {
            Job j = byId.get(p.processId());
            int turnaround = j.completion - p.arrivalTime();
            int waiting = turnaround - p.burstTime();
            results.add(new ProcessResult(p.processId(), p.arrivalTime(), p.burstTime(), p.priority(), j.completion, turnaround, waiting));
            wait += waiting;
            turn += turnaround;
        }
        return new SimulationResponse(algorithm, timeline, results, wait / jobs.size(), turn / jobs.size(), total);
    }

    private int roundRobin(List<Job> jobs, int quantum, List<Segment> timeline) {
        RingQueue<Job> ready = new RingQueue<>(jobs.size() + 1);
        int next = 0, time = 0, done = 0;
        while (done < jobs.size()) {
            while (next < jobs.size() && jobs.get(next).input.arrivalTime() <= time) ready.add(jobs.get(next++));
            if (ready.empty()) {
                time = jobs.get(next).input.arrivalTime();
                continue;
            }
            Job current = ready.remove();
            int run = Math.min(quantum, current.remaining);
            append(timeline, current.input.processId(), time, time + run);
            time += run;
            current.remaining -= run;
            while (next < jobs.size() && jobs.get(next).input.arrivalTime() <= time) ready.add(jobs.get(next++));
            if (current.remaining > 0) ready.add(current);
            else {
                current.completion = time;
                done++;
            }
        }
        return time;
    }

    private int priority(List<Job> jobs, List<Segment> timeline) {
        PriorityQueue<Job> ready = new PriorityQueue<>((a, b) -> {
            int c = Integer.compare(b.input.priority(), a.input.priority());
            if (c == 0) c = Integer.compare(a.input.arrivalTime(), b.input.arrivalTime());
            if (c == 0) c = Integer.compare(a.input.processId(), b.input.processId());
            return c;
        });
        int next = 0, time = 0, done = 0;
        while (done < jobs.size()) {
            while (next < jobs.size() && jobs.get(next).input.arrivalTime() <= time) ready.add(jobs.get(next++));
            if (ready.isEmpty()) {
                time = jobs.get(next).input.arrivalTime();
                continue;
            }
            Job current = ready.remove();
            append(timeline, current.input.processId(), time, time + 1);
            current.remaining--;
            time++;
            if (current.remaining == 0) {
                current.completion = time;
                done++;
            } else ready.add(current);
        }
        return time;
    }

    private void append(List<Segment> timeline, int id, int start, int end) {
        if (!timeline.isEmpty()) {
            Segment last = timeline.get(timeline.size() - 1);
            if (last.processId() == id && last.endTime() == start) {
                timeline.set(timeline.size() - 1, new Segment(id, last.startTime(), end));
                return;
            }
        }
        timeline.add(new Segment(id, start, end));
    }

    private static final class Job {
        final ProcessInput input;
        int remaining, completion;

        Job(ProcessInput input) {
            this.input = input;
            remaining = input.burstTime();
        }
    }

    private static final class RingQueue<T> {
        private final Object[] data;
        private int head, tail, size;

        RingQueue(int capacity) {
            data = new Object[Math.max(2, capacity)];
        }

        boolean empty() {
            return size == 0;
        }

        void add(T item) {
            if (size == data.length) throw new IllegalStateException("Queue full");
            data[tail] = item;
            tail = (tail + 1) % data.length;
            size++;
        }

        @SuppressWarnings("unchecked")
        T remove() {
            if (empty()) throw new NoSuchElementException();
            T item = (T) data[head];
            data[head] = null;
            head = (head + 1) % data.length;
            size--;
            return item;
        }
    }
}
