package naitsirc98.imagesplitter;

import java.util.ArrayList;

public final class SplittedImageList extends ArrayList<SplittedImage> {

	private static final long serialVersionUID = 1L;

	public SplittedImageList() {
		super();
	}
	
	public SplittedImageList(int size) {
		super(size);
	}

	public SplittedImage blend(int... indices) {
		
		SplittedImage result = SplittedImage.blend(get(indices[0], indices[indices.length-1]));
		
		removeRange(indices[0], indices[indices.length - 1]);
		
		set(indices[0], result);
		
		return result;
	}
	
	public void remove(int from, int to) {
		removeRange(from, to);
	}
	
	public SplittedImage[] get(int from, int to) {
		
		SplittedImage[] array = new SplittedImage[to-from+1];
		
		for(int i = from;i <= to;i++) {
			array[i-from] = get(i);
		}
		
		return array;
		
	}

}
