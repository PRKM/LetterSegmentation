package makeHisto;

import java.io.File;
import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

public class MakeHistogram {
	public static void mkList_v(Mat image, Mat origin, int flag, int index) {
		
		if(flag == 2){
			Imgproc.threshold(image, image, 50, 255, Imgproc.THRESH_BINARY);
			LoadImage.ShowBufferedImage(LoadImage.Mat2BufferedImage(image), "Changed");
		}
		
		Mat hist = Mat.zeros(image.rows(), image.cols(), 0);
		int Line, Dot;
		int[] coord_x, coord_y;
		int[] coord_xe, coord_ye;
		String Dir = null, Dir_Origin = null; // 저장할 폴더

		switch (flag) {
		case 0: {
			coord_x = new int[image.rows()];
			coord_y = new int[image.rows()];
			coord_xe = new int[image.rows()];
			coord_ye = new int[image.rows()];
			Line = image.rows();
			Dot = image.cols();
			Dir = Folders.VERTICALFOLDER.toString();
			Dir_Origin = Folders.ORIGINALVER.toString();
			break;
		}
		case 1: {
			coord_x = new int[image.cols()];
			coord_y = new int[image.cols()];
			coord_xe = new int[image.cols()];
			coord_ye = new int[image.cols()];
			Line = image.cols();
			Dot = image.rows();
			Dir = Folders.HORIZONTALFOLDER.toString();
			Dir_Origin = Folders.ORIGINALHOR.toString();
			break;
		}
		default:
			if (index < 0) {
				System.out.println("Error Occured!");
				return;
			}
			coord_x = new int[image.cols()];
			coord_y = new int[image.cols()];
			coord_xe = new int[image.cols()];
			coord_ye = new int[image.cols()];
			Line = image.cols();
			Dot = image.rows();
			Dir_Origin = Folders.ORIGINALRES.toString();
			break;
		}

		coord_x = initArray(Line);
		coord_y = initArray(Line);
		coord_xe = initArray(Line);
		coord_ye = initArray(Line);
		
		ArrayList<Integer> area_s = new ArrayList<Integer>();
		ArrayList<Integer> area_e = new ArrayList<Integer>();

		int dotCount = 0;
		int lineCount = 0;

		boolean areaStarted = false;

		for (int i = 0; i < Line; i++) {
			int count = 0;
			boolean lineStarted = false;
			for (int j = 0; j < Dot; j++) {
				double[] pix;
				if (flag == 0)
					pix = image.get(i, j);
				else
					pix = image.get(j, i);
				if(flag != 2){
					if (pix[0] >= 250) { // 추후 수정 할꺼임!! 이진화 이미지에 대한 처리로
						lineStarted = true;
						if (!areaStarted) {
							areaStarted = true;
							if (flag == 0) { // vertical
								coord_x[i] = 0;
								coord_y[i] = i;
							} else { // horizontal
								coord_x[i] = i;
								coord_y[i] = 0;
							}
							System.out.println("start : " + i);
							dotCount++;
						}
						count++;
					}
				} else {
					if (pix[0] == 0) { // 추후 수정 할꺼임!! 이진화 이미지에 대한 처리로
						lineStarted = true;
						if (!areaStarted) {
							areaStarted = true;
							if (flag == 0) { // vertical
								coord_x[i] = 0;
								coord_y[i] = i;
							} else { // horizontal
								coord_x[i] = i;
								coord_y[i] = 0;
							}
							System.out.println("start : " + i);
							dotCount++;
						}
						count++;
					}
				}
			}
			if(areaStarted)
				lineCount++;
			if (!lineStarted && areaStarted) { // 글자 구역이 끝난 경우
				areaStarted = false;
				// endP[i] = Point(image.cols, i-1);
				//System.out.println("End : " + i);
				if (flag == 0) {
					if (lineCount <= 5) {
						coord_x[i] = -1;
						coord_y[i] = -1;
						dotCount--;
						System.out.println("vertical Short : " + lineCount);
						lineCount -= lineCount;
					} else {
						coord_xe[i] = image.cols();
						coord_ye[i] = i;
						area_s.add(i - lineCount + 1);
						area_e.add(i);
						Imgproc.line(hist, new Point(0, i), new Point(hist.cols() - 1, i), Scalar.all(255), 1, 8, 0);
						System.out.println("end : " + i);
					}
				} else if(flag == 1){
					coord_xe[i] = i;
					coord_ye[i] = image.rows();
					area_s.add(i - lineCount);
					area_e.add(i);
					Imgproc.line(hist, new Point(i, 0), new Point(i, hist.rows() - 1), Scalar.all(255), 1, 8, 0);
				} else {
					if (lineCount == 0) {
						coord_x[i] = -1;
						coord_y[i] = -1;
						dotCount--;
						System.out.println("vertical Short : " + lineCount);
						lineCount -= lineCount;
					} else {
						coord_xe[i] = i;
						coord_ye[i] = image.rows();
						area_s.add(i - lineCount + 1);
						area_e.add(i);
						Imgproc.line(hist, new Point(0, i), new Point(hist.cols() - 1, i), Scalar.all(255), 1, 8, 0);
						System.out.println("end : " + i);
					}
				}
				lineCount -= lineCount;
			} else if (lineStarted && areaStarted) {
				if (flag == 0) {
					Imgproc.line(hist, new Point(0, i), new Point(count, i), Scalar.all(0), 1, 8, 0);
					Imgproc.line(hist, new Point(count, i), new Point(hist.cols() - 1, i), Scalar.all(255), 1, 8, 0);
					System.out.println("vertical connected(temp) : " + i);
				} else if (flag == 1) {
					if ((count < image.rows() * 0.02) || (count > image.rows() * 0.95)) {
						areaStarted = false;
						if ((lineCount <= 5) && (i == 0 || coord_x[i-1] == -1)) {
							coord_x[i] = -1;
							coord_y[i] = -1;
							dotCount--;
							lineCount -= lineCount;
						} else {
							coord_xe[i] = i;
							coord_ye[i] = image.rows();
							area_s.add(i - lineCount + 1);
							area_e.add(i);
							lineCount -= lineCount;
							//dotCount++;
						}
						Imgproc.line(hist, new Point(i, 0), new Point(i, hist.rows() - 1), Scalar.all(255), 1, 8, 0);
						continue;
					} else {
						Imgproc.line(hist, new Point(i, 0), new Point(i, count), Scalar.all(0), 1, 8, 0);
						Imgproc.line(hist, new Point(i, count), new Point(i, hist.rows() - 1), Scalar.all(255), 1, 8,
								0);
					}
				} else {
						Imgproc.line(hist, new Point(i, 0), new Point(i, count), Scalar.all(0), 1, 8, 0);
						Imgproc.line(hist, new Point(i, count), new Point(i, hist.rows() - 1), Scalar.all(255), 1, 8,
								0);
						System.out.println("letter connected(temp) : " + i);
				}
			} else {
				if (flag == 0)
					Imgproc.line(hist, new Point(0, i), new Point(hist.cols() - 1, i), Scalar.all(255), 1, 8, 0);
				else
					Imgproc.line(hist, new Point(i, 0), new Point(i, hist.rows() - 1), Scalar.all(255), 1, 8, 0);
			}
		}
		LoadImage.ShowBufferedImage(LoadImage.Mat2BufferedImage(hist), "Histogram" + flag);

		int[] result_x = new int[dotCount];
		int[] result_y = new int[dotCount];
		int[] result_xe = new int[dotCount];
		int[] result_ye = new int[dotCount];

		result_x = initArray(dotCount);
		result_y = initArray(dotCount);
		result_xe = initArray(dotCount);
		result_ye = initArray(dotCount);

		int ind = 0;

		for (int i = 0; i < area_s.size(); i++) {
			/*if (coord_x[i] >= 0 && coord_y[i] >= 0) {
				result_x[ind] = coord_x[i];
				result_y[ind] = coord_y[i];
				ind++;
			}*/
			result_x[i] = coord_x[area_s.get(i)];
			result_y[i] = coord_y[area_s.get(i)];
		}

//		ind = 0;

		for (int i = 0; i < area_e.size(); i++) {
			/*if (coord_xe[i] >= 0 && coord_ye[i] >= 0) {
				result_xe[ind] = coord_xe[i];
				result_ye[ind] = coord_ye[i];
				ind++;
			}*/
			result_xe[i] = coord_xe[area_e.get(i)];
			result_ye[i] = coord_ye[area_e.get(i)];
		}

		Point[] startP = new Point[dotCount];
		Point[] endP = new Point[dotCount]; // 각각 시작점과 끝점. 이미지 자르기용이다.

//		ind = 0;
		for (int i = 0; i < dotCount; i++) {
			if (flag == 0) {
				if (result_ye[i] - result_y[i] < 4)
					continue;
			} else {
				if (result_xe[i] - result_x[i] < 8)
					continue;
			}
			startP[ind] = new Point(result_x[i], result_y[i]);
			endP[ind] = new Point(result_xe[i], result_ye[i]);
			// 좌표값 설정하기
			ind++;
		}
		
		int filenum = 0;
		for (int i = 0; i < ind; i++) {
			boolean isfile = true;
			if (flag != 2) {
				while(isfile){
					if(!new File(Dir+"\\"+filenum+".jpg").isFile()){
						isfile = false;
						break;
					}
					filenum++;
				}
				String filePath = Dir + "\\" + filenum + ".jpg";
				Rect cut = new Rect(startP[i], endP[i]);
				Mat save = new Mat(image, cut);

				Imgcodecs.imwrite(filePath, save);
			} else {
				while(isfile){
					if(!new File(Dir_Origin+"\\"+filenum+".jpg").isFile()){
						isfile = false;
						break;
					}
					filenum++;
				}
			}
			String oriFilePath = Dir_Origin + "\\" + filenum + ".jpg";
			Rect cutOri = new Rect(startP[i], endP[i]);
			Mat saveOri = new Mat(origin, cutOri);

			Imgcodecs.imwrite(oriFilePath, saveOri);
			
			filenum++;
		}
		
		System.out.println(flag + " : is finished!!");
		
		int numFile;
		if (flag == 0) {
			numFile = new File("Sep_Hor").listFiles().length;
			if (index == numFile - 1) {
				String nextPath = Folders.ORIGINALVER.toString() + "\\0.jpg";
				String nextOriPath = Folders.ORIGINALVER.toString() + "\\0.jpg";
				Mat next = Imgcodecs.imread(nextPath);
				Mat nextOri = Imgcodecs.imread(nextOriPath);

				mkList_v(next, nextOri, 2, 0);
			} else {
				String nextPath = Folders.HORIZONTALFOLDER.toString() + "\\" + ++index + ".jpg";
				String nextOriPath = Folders.ORIGINALHOR.toString() + "\\" + index + ".jpg";
				Mat next = Imgcodecs.imread(nextPath);
				Mat nextOri = Imgcodecs.imread(nextOriPath);

				mkList_v(next, nextOri, 0, index);
			}
		} else if (flag == 1) {
			String nextPath = Folders.HORIZONTALFOLDER.toString() + "\\0.jpg";
			String nextOriPath = Folders.ORIGINALHOR.toString() + "\\0.jpg";
			Mat next = Imgcodecs.imread(nextPath);
			Mat nextOri = Imgcodecs.imread(nextOriPath);

			mkList_v(next, nextOri, 0, 0);
		} else if (flag == 2) {
			numFile = new File("Sep_Ver").listFiles().length;
			if (index == numFile - 1)
				return;
			else {
				String nextPath = Folders.ORIGINALVER.toString() + "\\" + ++index + ".jpg";
				String nextOriPath = Folders.ORIGINALVER.toString() + "\\" + index + ".jpg";
				Mat next = Imgcodecs.imread(nextPath);
				Mat nextOri = Imgcodecs.imread (nextOriPath);

				mkList_v(next, nextOri, 2, index);
			}
		}
	}

	private static int[] initArray(int length) {
		int[] temp = new int[length];
		for (int i = 0; i < length; i++) {
			temp[i] = -1;
		}
		return temp;
	}

	public static Mat[] Filterimg(Mat src) {
		Mat lab = new Mat();
		Imgproc.cvtColor(src, lab, Imgproc.COLOR_BGR2Lab);

		ArrayList<Mat> lab_planes = new ArrayList<Mat>();
		Core.split(lab, lab_planes);

		CLAHE clahe = Imgproc.createCLAHE();
		clahe.setClipLimit(4);
		Mat dst = new Mat();
		clahe.apply(lab_planes.get(0), dst);

		dst.copyTo(lab_planes.get(0));
		Core.merge(lab_planes, lab);

		Mat res = new Mat();
		Imgproc.cvtColor(lab, res, Imgproc.COLOR_Lab2BGR);

		Mat img_array = new Mat(), img_canny = new Mat(), img_thres = new Mat(), element;
		Mat[] result = new Mat[2];
		result[0] = new Mat();
		result[1] = new Mat();
		Imgproc.cvtColor(res, img_array, Imgproc.COLOR_BGR2GRAY);
		Imgproc.threshold(img_array, result[0], 0, 255, Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY);

		Imgproc.GaussianBlur(img_array, img_array, new Size(5, 5), 1.5);
		Imgproc.Canny(img_array, img_canny, 50, 200);
		Imgproc.threshold(img_canny, img_thres, 0, 255, Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY);

		element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(17, 5));
		Imgproc.morphologyEx(img_thres, result[1], Imgproc.MORPH_CLOSE, element);

		return result;
	}
}
