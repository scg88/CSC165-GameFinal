package a2;

import tage.*;
import org.joml.*;

public class Tile extends GameObject
{
	private int[] boardPos = new int[2];

	public Tile(int[] pos, ObjShape s, TextureImage t)
	{
		super(GameObject.root(), s, t);
		boardPos = pos;
	}
	
	public int[] getPos()
	{
		return boardPos;
	}
	public void setPos(int[] pos)
	{
		boardPos = pos;
	}
}