package My.coyote;

public class RequestInfo {

	
	// --------------- Constructors ---------------
	public RequestInfo( Request req) {
		this.req=req;
	}
	
	// ----------------- Instance Variables -----------------
	Request req;
    Response res;
    int stage = Constants.STAGE_NEW;
    String workerThreadName;
    
    
    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }
    
    
    
    public String getWorkerThreadName() {
        return workerThreadName;
    }
    
    public void setWorkerThreadName(String workerThreadName) {
        this.workerThreadName = workerThreadName;
    }
	
}
