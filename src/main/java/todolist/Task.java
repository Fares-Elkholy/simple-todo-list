package todolist;

public class Task {
    private String description;
    private boolean isDone;



    public Task(String desc){
        this.description = desc;
        this.isDone = false;
    }


    public String getDescription () {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public boolean isDone() {
        return isDone;
    }


    @Override
    public String toString() {
        return (isDone? "[x] " : "[ ] ") + description;
    }
}
