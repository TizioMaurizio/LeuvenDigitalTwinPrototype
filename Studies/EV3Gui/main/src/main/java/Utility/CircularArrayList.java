package Utility;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A simple ArrayList which you can iterate over by using a mod n arithmetic index
 * @param <T> is the type of the elements the list contains
 */
public class CircularArrayList<T> extends ArrayList <T>{
	@Override
	public T get(int index) {
		if (index<0)
			index=size()-index;
		if(index>=size())
			return super.get(index % size()); //index mod size (e.g. for array of 2 elems, index maps to 0,1,0,1,0,1...)
		/*else if (index<0
			return super.get(size()+index);//TODO check, even if useless*/
		else
			return super.get(index);

	}

	public List<T> asList(){
		return this.stream().collect(Collectors.toList());
	}
}
