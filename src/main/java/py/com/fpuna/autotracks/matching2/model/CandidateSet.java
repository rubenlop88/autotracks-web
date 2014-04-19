package py.com.fpuna.autotracks.matching2.model;

import java.util.List;

public class CandidateSet {

    private Point point;
    private List<Candidate> candidates;

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public List<Candidate> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<Candidate> candidates) {
        this.candidates = candidates;
    }

}
