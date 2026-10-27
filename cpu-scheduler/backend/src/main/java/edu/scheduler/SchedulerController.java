package edu.scheduler;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/scheduler")
@CrossOrigin(origins = "*")
public class SchedulerController {
    private final SchedulingService service;

    public SchedulerController(SchedulingService service) {
        this.service = service;
    }

    public record ProcessInput(int processId, int arrivalTime, int burstTime, int priority) {
    }

    public record SimulationRequest(String algorithm, Integer quantum, List<ProcessInput> processes) {
    }

    public record Segment(int processId, int startTime, int endTime) {
    }

    public record ProcessResult(int processId, int arrivalTime, int burstTime, int priority,
                                int completionTime, int turnaroundTime, int waitingTime) {
    }

    public record SimulationResponse(String algorithm, List<Segment> timeline,
                                     List<ProcessResult> processes, double averageWaitingTime,
                                     double averageTurnaroundTime, int totalTime) {
    }

    @PostMapping("/simulate")
    public SimulationResponse simulate(@RequestBody SimulationRequest request) {
        try {
            return service.simulate(request);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
