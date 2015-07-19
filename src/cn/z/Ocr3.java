package cn.z;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import cn.z.util.CommonUtil;

public class Ocr3 {
	
	private static String clazz = Ocr3.class.getSimpleName();
	private static int whiteThreshold = 100;
	
	public static BufferedImage removeBackgroud(String picFile)
			throws Exception {
		BufferedImage img = ImageIO.read(new File(picFile));
		img = img.getSubimage(1, 1, img.getWidth() - 2, img.getHeight() - 2);
		int width = img.getWidth();
		int height = img.getHeight();
		double subWidth = width / 5.0;
		for (int i = 0; i < 4; i++) {
			Map<Integer, Integer> map = new HashMap<Integer, Integer>();
			for (int x = (int) (1 + i * subWidth); x < (i + 1) * subWidth
					&& x < width - 1; ++x) {
				for (int y = 0; y < height; ++y) {
					if (CommonUtil.isWhite(img.getRGB(x, y),600) == 1)
						continue;
					if (map.containsKey(img.getRGB(x, y))) {
						map.put(img.getRGB(x, y), map.get(img.getRGB(x, y)) + 1);
					} else {
						map.put(img.getRGB(x, y), 1);
					}
				}
			}
			int max = 0;
			int colorMax = 0;
			for (Integer color : map.keySet()) {
				if (max < map.get(color)) {
					max = map.get(color);
					colorMax = color;
				}
			}
			for (int x = (int) (1 + i * subWidth); x < (i + 1) * subWidth
					&& x < width - 1; ++x) {
				for (int y = 0; y < height; ++y) {
					if (img.getRGB(x, y) != colorMax) {
						img.setRGB(x, y, Color.WHITE.getRGB());
					} else {
						img.setRGB(x, y, Color.BLACK.getRGB());
					}
				}
			}
		}
		return img;
	}
	public static String getAllOcr(String file) throws Exception {
		BufferedImage img = removeBackgroud(file);
		List<BufferedImage> listImg = splitImage(img);
		Map<BufferedImage, String> map = CommonUtil.loadTrainData(clazz);
		String result = "";
		for (BufferedImage bi : listImg) {
			result += getSingleCharOcr(bi, map);
		}
		ImageIO.write(img, "JPG", new File("result/"+clazz + "/"+result+".jpg"));
		return result;
	}
	
	private static String getSingleCharOcr(BufferedImage img,
			Map<BufferedImage, String> map) {
		String result = "#";
		int width = img.getWidth();
		int height = img.getHeight();
		int min = width * height;
		for (BufferedImage bi : map.keySet()) {
			int count = 0;
			if (Math.abs(bi.getWidth()-width) > 2)
				continue;
			int widthmin = width < bi.getWidth() ? width : bi.getWidth();
			int heightmin = height < bi.getHeight() ? height : bi.getHeight();
			Label1: for (int x = 0; x < widthmin; ++x) {
				for (int y = 0; y < heightmin; ++y) {
					if (CommonUtil.isWhite(img.getRGB(x, y),whiteThreshold) != CommonUtil.isWhite(bi.getRGB(x, y),whiteThreshold)) {
						count++;
						if (count >= min)
							break Label1;
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
	
	private static List<BufferedImage> splitImage(BufferedImage img)
			throws Exception {
		List<BufferedImage> subImgs = new ArrayList<BufferedImage>();
		int width = img.getWidth();
		int height = img.getHeight();
		List<Integer> weightlist = new ArrayList<Integer>();
		for (int x = 0; x < width; ++x) {
			int count = 0;
			for (int y = 0; y < height; ++y) {
				if (CommonUtil.isWhite(img.getRGB(x, y),whiteThreshold) == 0) {
					count++;
				}
			}
			weightlist.add(count);
		}
		for (int i = 0; i < weightlist.size();i++) {
			int length = 0;
			while (i < weightlist.size() && weightlist.get(i) > 0) {
				i++;
				length++;
			}
			if (length > 2) {
				subImgs.add(CommonUtil.removeBlank(img.getSubimage(i - length, 0,
						length, height),whiteThreshold,0));
			}
		}
		return subImgs;
	}
	
	public static void main(String[] args) throws Exception {
		//---step1 downloadImage
//		String url = "http://game.tom.com/checkcode.php";
//		//下载图片
//		CommonUtil.downloadImage(url, clazz);
		new File("img/"+clazz).mkdirs();
		new File("train/"+clazz).mkdirs();
		new File("result/"+clazz).mkdirs();
		//先删除result/ocr目录，开始识别
		for (int i = 0; i < 30; ++i) {
			String text = getAllOcr("img/"+clazz+"/" + i + ".jpg");
			System.out.println(i + ".jpg = " + text);
		}
	}

}
