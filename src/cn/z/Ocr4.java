package cn.z;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;

import com.jhlabs.image.ScaleFilter;

import cn.z.svm.svm_predict;
import cn.z.util.CommonUtil;

public class Ocr4 {

	private static String clazz = Ocr4.class.getSimpleName();
	private static Map<BufferedImage, String> trainMap = null;
	private static int whiteThreshold = 300;
	private static boolean useSvm = true;

	public static int getColorBright(int colorInt) {
		Color color = new Color(colorInt);
		return color.getRed() + color.getGreen() + color.getBlue();

	}

	public static int isBlackOrWhite(int colorInt) {
		if (getColorBright(colorInt) < 30 || getColorBright(colorInt) > 730) {
			return 1;
		}
		return 0;
	}

	public static BufferedImage removeBackgroud(String picFile) throws Exception {
		BufferedImage img = ImageIO.read(new File(picFile));
		int width = img.getWidth();
		int height = img.getHeight();
		for (int x = 1; x < width - 1; ++x) {
			for (int y = 1; y < height - 1; ++y) {
				if (getColorBright(img.getRGB(x, y)) < 100) {
					if (isBlackOrWhite(img.getRGB(x - 1, y)) + isBlackOrWhite(img.getRGB(x + 1, y))
							+ isBlackOrWhite(img.getRGB(x, y - 1)) + isBlackOrWhite(img.getRGB(x, y + 1)) == 4) {
						img.setRGB(x, y, Color.WHITE.getRGB());
					}
				}
			}
		}
		for (int x = 1; x < width - 1; ++x) {
			for (int y = 1; y < height - 1; ++y) {
				if (getColorBright(img.getRGB(x, y)) < 100) {
					if (isBlackOrWhite(img.getRGB(x - 1, y)) + isBlackOrWhite(img.getRGB(x + 1, y))
							+ isBlackOrWhite(img.getRGB(x, y - 1)) + isBlackOrWhite(img.getRGB(x, y + 1)) == 4) {
						img.setRGB(x, y, Color.WHITE.getRGB());
					}
				}
			}
		}
		img = img.getSubimage(1, 1, img.getWidth() - 2, img.getHeight() - 2);
		return img;
	}

	public static List<BufferedImage> splitImage(BufferedImage img) throws Exception {
		List<BufferedImage> subImgs = new ArrayList<BufferedImage>();
		int width = img.getWidth();
		int height = img.getHeight();
		List<Integer> weightlist = new ArrayList<Integer>();
		for (int x = 0; x < width; ++x) {
			int count = 0;
			for (int y = 0; y < height; ++y) {
				if (CommonUtil.isWhite(img.getRGB(x, y), whiteThreshold) == 0) {
					count++;
				}
			}
			weightlist.add(count);
		}
		for (int i = 0; i < weightlist.size(); i++) {
			int length = 0;
			while (i < weightlist.size() && weightlist.get(i) > 0) {
				i++;
				length++;
			}
			if (length > 18) {
				subImgs.add(
						CommonUtil.removeBlank(img.getSubimage(i - length, 0, length / 2, height), whiteThreshold, 0));
				subImgs.add(CommonUtil.removeBlank(img.getSubimage(i - length / 2, 0, length / 2, height),
						whiteThreshold, 0));
			} else if (length > 5) {
				subImgs.add(CommonUtil.removeBlank(img.getSubimage(i - length, 0, length, height), whiteThreshold, 0));
			}
		}

		return subImgs;
	}

	public static boolean isNotEight(BufferedImage img) {
		int width = img.getWidth();
		int height = img.getHeight();
		int minCount = width;
		for (int y = height / 2 - 2; y < height / 2 + 2; ++y) {
			int count = 0;
			for (int x = 0; x < width / 2 + 2; ++x) {
				if (CommonUtil.isWhite(img.getRGB(x, y), whiteThreshold) == 0) {
					count++;
				}
			}
			minCount = Math.min(count, minCount);
		}
		return minCount < 2;
	}

	public static boolean isNotThree(BufferedImage img) {
		int width = img.getWidth();
		int height = img.getHeight();
		int minCount = width;
		for (int y = height / 2 - 3; y < height / 2 + 3; ++y) {
			int count = 0;
			for (int x = 0; x < width / 2 + 1; ++x) {
				if (CommonUtil.isWhite(img.getRGB(x, y), whiteThreshold) == 0) {
					count++;
				}
			}
			minCount = Math.min(count, minCount);
		}
		return minCount > 0;
	}

	public static boolean isNotFive(BufferedImage img) {
		int width = img.getWidth();
		int height = img.getHeight();
		int minCount = width;
		for (int y = 0; y < height / 3; ++y) {
			int count = 0;
			for (int x = width * 2 / 3; x < width; ++x) {
				if (CommonUtil.isWhite(img.getRGB(x, y), whiteThreshold) == 0) {
					count++;
				}
			}
			minCount = Math.min(count, minCount);
		}
		return minCount > 0;
	}

	public static String getSingleCharOcr(BufferedImage img, Map<BufferedImage, String> map) throws Exception {
		if (useSvm) {
			String input = new File("img/" + clazz + "/input.txt").getAbsolutePath();
			String output = new File("result/" + clazz + "/output.txt").getAbsolutePath();
			CommonUtil.imgToSvmInput(img, input, whiteThreshold);
			svm_predict.main(
					new String[] { input, new File("train/" + clazz + "/data.txt.model").getAbsolutePath(), output });
			List<String> predict = IOUtils.readLines(new FileInputStream(output));
			if (predict.size() > 0 && predict.get(0).length() > 0) {
				return predict.get(0).substring(0, 1);
			}
			return "#";
		}

		String result = "#";
		img = scaleImage(img);
		int width = img.getWidth();
		int height = img.getHeight();
		int min = width * height;
		boolean bNotEight = isNotEight(img);
		boolean bNotThree = isNotThree(img);
		boolean bNotFive = isNotFive(img);
		for (BufferedImage bi : map.keySet()) {
			if (bNotThree && map.get(bi).startsWith("3"))
				continue;
			if (bNotEight && map.get(bi).startsWith("8"))
				continue;
			if (bNotFive && map.get(bi).startsWith("5"))
				continue;
			double count1 = getBlackCount(img);
			double count2 = getBlackCount(bi);
			if (Math.abs(count1 - count2) / Math.max(count1, count2) > 0.25)
				continue;
			int count = 0;
			if (width < bi.getWidth() && height < bi.getHeight()) {
				for (int m = 0; m <= bi.getWidth() - width; m++) {
					for (int n = 0; n <= bi.getHeight() - height; n++) {
						Label1: for (int x = m; x < m + width; ++x) {
							for (int y = n; y < n + height; ++y) {
								if (CommonUtil.isWhite(img.getRGB(x - m, y - n), whiteThreshold) != CommonUtil
										.isWhite(bi.getRGB(x, y), whiteThreshold)) {
									count++;
									if (count >= min)
										break Label1;
								}
							}
						}
					}
				}
			} else {
				int widthmin = width < bi.getWidth() ? width : bi.getWidth();
				int heightmin = height < bi.getHeight() ? height : bi.getHeight();
				Label1: for (int x = 0; x < widthmin; ++x) {
					for (int y = 0; y < heightmin; ++y) {
						if (CommonUtil.isWhite(img.getRGB(x, y), whiteThreshold) != CommonUtil.isWhite(bi.getRGB(x, y),
								whiteThreshold)) {
							count++;
							if (count >= min)
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

	public static String getAllOcr(String file) throws Exception {
		BufferedImage img = removeBackgroud(file);
		List<BufferedImage> listImg = splitImage(img);
		Map<BufferedImage, String> map = loadTrainData();
		String result = useSvm ? "svm_" : "";
		for (BufferedImage bi : listImg) {
			result += getSingleCharOcr(bi, map);
		}
		System.out.println(result);
		ImageIO.write(img, "JPG", new File("result/" + clazz + "/" + result + ".jpg"));
		return result;
	}

	public static Map<BufferedImage, String> loadTrainData() throws Exception {
		if (trainMap == null) {
			Map<BufferedImage, String> map = new HashMap<BufferedImage, String>();
			File dir = new File("train/" + clazz);
			File[] files = dir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".jpg");
				}
			});
			for (File file : files) {
				map.put(scaleImage(ImageIO.read(file)), file.getName().charAt(0) + "");
			}
			trainMap = map;
		}
		return trainMap;
	}

	public static int getBlackCount(BufferedImage img) {
		int width = img.getWidth();
		int height = img.getHeight();
		int count = 0;
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				if (CommonUtil.isWhite(img.getRGB(x, y), whiteThreshold) == 0) {
					count++;
				}
			}
		}
		return count;
	}

	public static BufferedImage scaleImage(BufferedImage img) {
		ScaleFilter sf = new ScaleFilter(16, 16);
		BufferedImage imgdest = new BufferedImage(16, 16, img.getType());
		return sf.filter(img, imgdest);
	}

	public static void main(String[] args) throws Exception {
		// ---step1 downloadImage
		// String url = "http://reg.keepc.com/getcode/getCode.php";
		// 下载图片
		// CommonUtil.downloadImage(url, clazz);
		new File("img/" + clazz).mkdirs();
		new File("train/" + clazz).mkdirs();
		new File("result/" + clazz).mkdirs();
		// 先删除result/ocr目录，开始识别
		for (int i = 0; i < 30; ++i) {
			String text = getAllOcr("img/" + clazz + "/" + i + ".jpg");
			System.out.println(i + ".jpg = " + text);
		}

//		CommonUtil.scaleTraindata(clazz, whiteThreshold);
//		svm_train train = new svm_train();
//		train.run(new String[] { new File("train/" + clazz + "/data.txt").getAbsolutePath(),
//				new File("train/" + clazz + "/data.txt.model").getAbsolutePath() });
	}

}
