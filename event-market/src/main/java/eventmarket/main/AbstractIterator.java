package eventmarket.main;


public abstract class AbstractIterator<T> {
    public abstract T next();
    public abstract int getSteps();
    public abstract void setSteps(int step);
    public abstract boolean isDone();
    public abstract T current();
    public abstract T first();
}
