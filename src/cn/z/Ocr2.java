package cn.z;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import cn.z.util.CommonUtil;

public class Ocr2 {

	private static String clazz = Ocr2.class.getSimpleName();
	private static int whiteThreshold = 100;

	public static String getAllOcr(String file) throws Exception {
		final BufferedImage img = CommonUtil.removeBackgroud(file, whiteThreshold);
		final List<BufferedImage> listImg = splitImage(img);
		final Map<BufferedImage, String> map = CommonUtil.loadTrainData(clazz);
		String result = "";
		for (final BufferedImage bi : listImg) {
			result += getSingleCharOcr(bi, map);
		}
		ImageIO.write(img, "JPG", new File("result/" + clazz + "/" + result + ".jpg"));
		return result;
	}

	private static String getSingleCharOcr(BufferedImage img, Map<BufferedImage, String> map) {
		String result = "";
		final int width = img.getWidth();
		final int height = img.getHeight();
		int min = width * height;
		for (final BufferedImage bi : map.keySet()) {
			int count = 0;
			final int widthmin = width < bi.getWidth() ? width : bi.getWidth();
			final int heightmin = height < bi.getHeight() ? height : bi.getHeight();
			Label1: for (int x = 0; x < widthmin; ++x) {
				for (int y = 0; y < heightmin; ++y) {
					if (CommonUtil.isWhite(img.getRGB(x, y), whiteThreshold) != CommonUtil.isWhite(bi.getRGB(x, y),
							whiteThreshold)) {
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
		final int width = img.getWidth();
		final int height = img.getHeight();
		final List<Integer> weightlist = new ArrayList<Integer>();
		for (int x = 0; x < width; ++x) {
			int count = 0;
			for (int y = 0; y < height; ++y) {
				if (CommonUtil.isWhite(img.getRGB(x, y), whiteThreshold) == 1) {
					count++;
				}
			}
			weightlist.add(count);
		}
		for (int i = 0; i < weightlist.size();) {
			int length = 0;
			while (weightlist.get(i++) > 1) {
				length++;
			}
			if (length > 12) {
				subImgs.add(CommonUtil.removeBlank(img.getSubimage(i - length - 1, 0, length / 2, height),
						whiteThreshold, 1));
				subImgs.add(CommonUtil.removeBlank(img.getSubimage(i - length / 2 - 1, 0, length / 2, height),
						whiteThreshold, 1));
			} else if (length > 3) {
				subImgs.add(
						CommonUtil.removeBlank(img.getSubimage(i - length - 1, 0, length, height), whiteThreshold, 1));
			}
		}
		return subImgs;
	}

	public static void main(String[] args) throws Exception {
		new File("img/" + clazz).mkdirs();
		new File("train/" + clazz).mkdirs();
		new File("result/" + clazz).mkdirs();

		for (int i = 0; i < 30; ++i) {
			final String text = getAllOcr("img/" + clazz + "/" + i + ".jpg");
			System.out.println(i + ".jpg = " + text);
		}
	}

}
