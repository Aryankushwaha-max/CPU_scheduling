# CPU Task Scheduling Simulator

Spring Boot 3 / Java 17 backend and plain HTML/CSS/JavaScript frontend.

## Run
1. Install Java 17 and Maven.
2. In `backend`, run `mvn spring-boot:run`.
3. Open `frontend/index.html` in a browser.

The browser calls `POST http://localhost:8080/api/scheduler/simulate`. For request JSON, set `algorithm` to `ROUND_ROBIN` (and `quantum`) or `PRIORITY`. Larger priority numbers are higher. Priority scheduling is preemptive at one-unit boundaries.

The simulation uses a linked list for process records, a circular queue for Round Robin, and a priority queue for preemptive priority. Metrics include each process completion, turnaround, waiting time, plus averages.
