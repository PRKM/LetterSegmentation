package makeHisto;

import java.io.File;
import java.util.Scanner;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

enum Folders {
	VERTICALFOLDER("Sep_Ver"), HORIZONTALFOLDER("Sep_Hor"),
	ORIGINALVER("Ori_Ver"), ORIGINALHOR("Ori_Hor"), ORIGINALRES("Ori_Res");

	private final String text;

	private Folders(final String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return text;
	}
}

public class Main {
	
	static{System.loadLibrary(Core.NATIVE_LIBRARY_NAME);}

	public static void main(String[] args) {
		
		Mat target;
		String impath;
		
		selectDirectory(0);  // temporary method. if it merges with android, it may be removed.

		System.out.print("Drag Image Here : ");
		Scanner s = new Scanner(System.in);
		impath = s.nextLine();
		System.out.println(impath);
		target = Imgcodecs.imread(impath);

		if (target.empty()) {
			System.out.println("Can't load Image");
			s.close();
			return;
		}

		System.out.println("Width : " + target.cols());
		System.out.println("Height : " + target.rows());

		LoadImage.ShowBufferedImage(LoadImage.Mat2BufferedImage(target), impath);
		
		Mat[] result = new Mat[2];
		
		result = MakeHistogram.Filterimg(target);
		
		LoadImage.ShowBufferedImage(LoadImage.Mat2BufferedImage(result[0]), "A");
		LoadImage.ShowBufferedImage(LoadImage.Mat2BufferedImage(result[1]), "B");

		MakeHistogram.mkList_v(result[1], result[0], 1, -1);

		s.close();
	}
	
	private static void createDirectory(String DirName){
		File Dir = new File(DirName);
		if(Dir.mkdir())
			System.out.println("Folder " + DirName + " created!");
		else
			System.out.println("Folder " + DirName + " is already created!");
	}
	
	private static void selectDirectory(int flag){
		if (flag == 0)
		{
			createDirectory(Folders.VERTICALFOLDER.toString());
			deleteOldImages(Folders.VERTICALFOLDER.toString());
		}
		else if(flag == 1)
		{
			createDirectory(Folders.HORIZONTALFOLDER.toString());
			deleteOldImages(Folders.HORIZONTALFOLDER.toString());
		}
		else if(flag == 2)
		{
			createDirectory(Folders.ORIGINALHOR.toString());
			deleteOldImages(Folders.ORIGINALHOR.toString());
		}
		else if(flag == 3)
		{
			createDirectory(Folders.ORIGINALVER.toString());
			deleteOldImages(Folders.ORIGINALVER.toString());
		}
		else
		{
			createDirectory(Folders.ORIGINALRES.toString());
			deleteOldImages(Folders.ORIGINALRES.toString());
		}
	}
	
	private static void deleteOldImages(String directory){
		File oldImages = new File(directory);

		File[] tempFile = oldImages.listFiles();
		
		if(tempFile.length > 0){
			for (int i = 0; i < tempFile.length; i++){
				if(tempFile[i].isFile())
					tempFile[i].delete();
			}
		}
		
		if(directory.equals(Folders.VERTICALFOLDER.toString()))
			selectDirectory(1);
		else if(directory.equals(Folders.HORIZONTALFOLDER.toString()))
			selectDirectory(2);
		else if(directory.equals(Folders.ORIGINALHOR.toString()))
			selectDirectory(3);
		else if(directory.equals(Folders.ORIGINALVER.toString()))
			selectDirectory(4);
	}
}