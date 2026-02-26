package uk.co.epicuri.waiter.loaders;

public class LoaderWrapper<E> {
	private final E payload;
	private final boolean error;
	
	public LoaderWrapper(E payload){
		this.payload = payload;
		this.error = false;
	}
	
	private LoaderWrapper(){
		payload = null;
		error = true;
	}
	
	public static <E> LoaderWrapper<E> ERROR() {
		return new LoaderWrapper<E>();
	}
	
	public E getPayload(){
		return payload;
	}
	public boolean isError(){
		return error;
	}
}
