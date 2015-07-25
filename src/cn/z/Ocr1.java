package cn.z;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;

import cn.z.svm.svm_predict;
import cn.z.util.CommonUtil;

public class Ocr1 {

	private static String clazz = Ocr1.class.getSimpleName();
	private static int whiteThreshold = 100;
	private static boolean useSvm = true;

	public static String getAllOcr(String file) throws Exception {
		final BufferedImage img = CommonUtil.removeBackgroud(file, whiteThreshold);
		final List<BufferedImage> listImg = splitImage(img);
		final Map<BufferedImage, String> map = CommonUtil.loadTrainData(clazz);
		String result = useSvm ? "svm_" : "";
		for (final BufferedImage bi : listImg) {
			result += getSingleCharOcr(bi, map);
		}
		ImageIO.write(img, "JPG", new File("result/" + clazz + "/" + result + ".jpg"));
		return result;
	}

	private static String getSingleCharOcr(BufferedImage img, Map<BufferedImage, String> map) throws Exception {
		if (useSvm) {
			final String input = new File("img/" + clazz + "/input.txt").getAbsolutePath();
			final String output = new File("result/" + clazz + "/output.txt").getAbsolutePath();
			CommonUtil.imgToSvmInput(img, input, whiteThreshold);
			svm_predict.main(
					new String[] { input, new File("train/" + clazz + "/data.txt.model").getAbsolutePath(), output });
			final List<String> predict = IOUtils.readLines(new FileInputStream(output));
			if (predict.size() > 0 && predict.get(0).length() > 0) {
				return predict.get(0).substring(0, 1);
			}
			return "#";
		}
		String result = "";
		final int width = img.getWidth();
		final int height = img.getHeight();
		int min = width * height;
		for (final BufferedImage bi : map.keySet()) {
			int count = 0;
			Label1: for (int x = 0; x < width; ++x) {
				for (int y = 0; y < height; ++y) {
					if (img.getRGB(x, y) != bi.getRGB(x, y)) {
						count++;
						if (count >= min) {
							break Label1;
						}
					}
				}
			}
			if (count < min) {
				min = count;
				result = map.get(bi);
			}
		}
		return result;
	}

	private static List<BufferedImage> splitImage(BufferedImage img) throws Exception {
		final List<BufferedImage> subImgs = new ArrayList<BufferedImage>();
		subImgs.add(img.getSubimage(10, 6, 8, 10));
		subImgs.add(img.getSubimage(19, 6, 8, 10));
		subImgs.add(img.getSubimage(28, 6, 8, 10));
		subImgs.add(img.getSubimage(37, 6, 8, 10));
		return subImgs;
	}

	public static void main(String[] args) throws Exception {
		// ---step1 downloadImage
		// String url = "http://www.puke888.com/authimg.php";
		// 下载图片
		// CommonUtil.downloadImage(url, clazz);
		new File("img/" + clazz).mkdirs();
		new File("train/" + clazz).mkdirs();
		new File("result/" + clazz).mkdirs();
		// 先删除result/ocr目录，开始识别
		for (int i = 0; i < 30; ++i) {
			final String text = getAllOcr("img/" + clazz + "/" + i + ".jpg");
			System.out.println(i + ".jpg = " + text);
		}

		// CommonUtil.scaleTraindata(clazz, whiteThreshold);
		// svm_train train = new svm_train();
		// train.run(new String[] { new File("train/" + clazz +
		// "/data.txt").getAbsolutePath(),
		// new File("train/" + clazz + "/data.txt.model").getAbsolutePath() });
	}

}
