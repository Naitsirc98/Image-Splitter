package naitsirc98.imagesplitter;

public final class SplittedImage implements Comparable<SplittedImage> {
	
	public static SplittedImage min(SplittedImage a, SplittedImage b) {
		return a.compareTo(b) < 0 ? a : b;
	}	
	
	public static SplittedImage max(SplittedImage a, SplittedImage b) {
		return a.compareTo(b) > 0 ? a : b;
	}
	
	public static SplittedImage blend(SplittedImage... bounds) {
		
		if(bounds == null || bounds.length == 0) {
			throw new IllegalArgumentException();
		}
		
		final SplittedImage result = bounds[0].clone();
		
		for(int i = 1;i < bounds.length;i++) {
			
			final SplittedImage b = bounds[i];
			
			if(!bounds[i-1].isConsecutive(b)) {
				throw new IllegalStateException("Images are not consecutive: "+bounds[i-1]+", "+b);
			}
			
			if(b.x < result.x) {
				result.x = b.x;
				result.width += result.x - b.x;
			} else if(b.x >= result.x) {
				result.width = Math.max(result.width, b.x+b.width-result.x);
			}
			
			if(b.y < result.y) {
				result.y = b.y;
				result.height += result.y - b.y;
			} else if(b.y >= result.y) {
				result.height = Math.max(result.height, b.y+b.height-result.y);
			}
			
			
		}
		
		return result;
	}
	
	int x, y;
	int width = 1, height = 1;
	int row = -1, column = -1;
	int id;
	
	public SplittedImage() {
		x = y = 0;
	}
	
	public SplittedImage(int x, int y) {
		this(x, y, 1, 1);
	}
	
	public SplittedImage(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public boolean intersects(SplittedImage other) {
		
		if(other.width <= 0 || other.height <= 0 || width <= 0 || height <= 0) {
			return false;
		}
		
		final int ow = other.width + other.x;
		final int oh = other.height + other.y;
		final int w = width + x;
		final int h = height + y;
		
		return ((ow < other.x || ow > x) &&
				(oh < other.y || oh > y) &&
				       (w < x || w > other.x) &&
				       (h < y || h > other.y));
		
	}
	
	public boolean contains(int x, int y) {
		
		int w = width;
		int h = height;
		
		if((w | h) < 0) {
			return false;
		}
		
		if(x < this.x || y < this.y) {
			return false;
		}
		
		w += this.x;
		h += this.y;
		
		return ((w < this.x || w > x) && 
				(h < this.y || h > y));
		
	}
	
	public int distance(SplittedImage other) {
		return (int) Math.sqrt(Math.pow(x-other.x, 2) + Math.pow(y-other.y,2));
	}
	
	public boolean isConsecutive(SplittedImage other) {
		
		final int dr = Math.abs(row - other.row);
		final int dc = Math.abs(column - other.column);
		
		return dr == 1 || dc == 1;
		
	}
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
	
	public int getID() {
		return id;
	}
	
	public int getCenterX() {
		return (x + width) / 2;
	}
	
	public int getCenterY() {
		return (y + height) / 2;
	}

	public int getRow() {
		return row;
	}

	public int getColumn() {
		return column;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getSize() {
		return width*height;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + height;
		result = prime * result + width;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof SplittedImage))
			return false;
		SplittedImage other = (SplittedImage) obj;
		if (height != other.height)
			return false;
		if (width != other.width)
			return false;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ImageBounds [x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + "]";
	}

	@Override
	public int compareTo(SplittedImage other) {
		return Integer.compare(getSize(), other.getSize());
	}
	
	@Override
	public SplittedImage clone() {
		final SplittedImage clone = new SplittedImage(x,y,width,height);
		clone.id = id;
		clone.row = row;
		clone.column = column;
		return clone;
	}

}
