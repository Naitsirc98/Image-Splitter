package naitsirc98.imagesplitter;

import java.util.ArrayList;

/**
 * An {@code ArrayList} of {@code ImageBounds}. 
 * 
 * <p>It just add three additional methods:</p>
 * 
 * <ul>
 * 
 * <p>{@code blend}: combine two or more subimages to make only one. Useful for subimages that have particles</p>
 * <p>{@code removeRange}: removes the elements in the given range</p>
 * <p>{@code getRange}: gets elements in the given range as an array</p>
 * </ul>
 * 
 * 
 * */
public final class SplittedImageList extends ArrayList<ImageBounds> {

	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor
	 * */
	public SplittedImageList() {
		super();
	}
	
	/**
	 * Constructs a {@code SplittedImageList} with the specified initial capacity.
	 * 
	 * @param capacity the initial capacity
	 **/
	public SplittedImageList(int capacity) {
		super(capacity);
	}

	/**
	 * Combines two or more subimages into one, and replaces the old independent subimages by
	 * the new one. The subimages must be consecutive.
	 * 
	 * @param indices the range of subimages
	 * 
	 * @return the result subimage
	 * 
	 * */
	public ImageBounds blend(int... indices) {
		
		ImageBounds result = ImageBounds.blend(getRange(indices[0], indices[indices.length-1]));
		
		removeRange(indices[0], indices[indices.length - 1]);
		
		set(indices[0], result);
		
		return result;
	}
	
	/**
	 * Removes the elements that are between from and to, both included.
	 * from must be <= to.
	 * 
	 * @param from start index
	 * @param to end index
	 * 
	 * */
	public void removeRange(int from, int to) {
		super.removeRange(from, to+1);
	}
	
	/**
	 * Gets the elements that are between from and to, both included.
	 * from must be <= to.
	 * 
	 * @param from start index
	 * @param to end index
	 * 
	 * @return the elements in the range [from, to] as an array
	 * 
	 **/
	public ImageBounds[] getRange(int from, int to) {
		
		ImageBounds[] array = new ImageBounds[to-from+1];
		
		for(int i = from;i <= to;i++) {
			array[i-from] = get(i);
		}
		
		return array;
		
	}

}
