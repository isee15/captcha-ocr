package cn.z;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import cn.z.util.FontGlyphs;
import cn.z.util.ImageUtil;

public class Pwntcha_Xanga {
	//getRaster can speed
	public static BufferedImage fill_white_holes(BufferedImage img) {
		int x, y;
		final int w = img.getWidth();
		final int h = img.getHeight();
		final BufferedImage newImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

		for (y = 0; y < h; y++) {
			for (x = 0; x < w; x++) {
				newImg.setRGB(x, y, img.getRGB(x, y));
			}
		}
		for (y = 1; y < h - 1; y++) {
			for (x = 1; x < w - 1; x++) {
				int count = 0;
				Color color = new Color(img.getRGB(x, y));
				if (color.getRed() <= 16) {
					continue;
				}
				color = new Color(img.getRGB(x + 1, y));
				count += color.getRed();
				color = new Color(img.getRGB(x - 1, y));
				count += color.getRed();
				color = new Color(img.getRGB(x, y + 1));
				count += color.getRed();
				color = new Color(img.getRGB(x, y - 1));
				count += color.getRed();
				if (count > 600) {
					continue;
				}
				newImg.setRGB(x, y, new Color(count / 5, count / 5, count / 5).getRGB());
			}
		}
		return newImg;
	}

	public static void main(String[] args) throws Exception {

		final String fontFile = "img/xanga/x_freemonobold_32_az.bmp";
		final HashMap<BufferedImage, FontGlyphs> fontMap = ImageUtil.loadFontFixed(fontFile,
				"abcdefghijklmnopqrstuvwxyz");
		fontMap.putAll(ImageUtil.loadFontVariable("img/xanga/x_freemonobold_24_az.bmp", "abcdefghijklmnopqrstuvwxyz"));
		fontMap.putAll(ImageUtil.loadFontVariable("img/xanga/x_freesansbold_32_az.bmp", "abcdefghijklmnopqrstuvwxyz"));
		fontMap.putAll(ImageUtil.loadFontVariable("img/xanga/x_comic_32_az.bmp", "abcdefghijklmnopqrstuvwxyz"));
		fontMap.putAll(ImageUtil.loadFontVariable("img/xanga/x_comic_24_az_messed.bmp", "abcdefghijklmnopqrstuvwxyz"));
		fontMap.putAll(
				ImageUtil.loadFontVariable("img/xanga/x_freesansbold_36_az_messed.bmp", "abcdefghijklmnopqrstuvwxyz"));

		for (int k = 0; k < 100; k++) {
			final StringBuilder result = new StringBuilder();
			final String picFile = String.format("img/xanga/xanga_%03d.jpeg", k);
			BufferedImage img = ImageIO.read(new File(picFile));
			img = ImageUtil.filterContrast(img);
			img = fill_white_holes(img);
			img = ImageUtil.filterSmooth(img);
			img = ImageUtil.filterContrast(img);

			int x, y;
			int r;
			int xmin, xmax, ymin, ymax, cur = 0;
			int bestdist;
			BufferedImage bestch = null;
			final int w = img.getWidth();
			final int h = img.getHeight();
	
			while (cur < 6) {
				/* Try to find 1st letter */
				bestdist = Integer.MAX_VALUE;
				for (final Entry<BufferedImage, FontGlyphs> entry : fontMap.entrySet()) {
					int localmin = Integer.MAX_VALUE;

					final BufferedImage fontImg = entry.getKey();
					if (entry.getValue().c.equals("l") || entry.getValue().c.equals("z")) {
						continue;
					}
					xmin = -5;
					ymin = -3;
					xmax = fontImg.getWidth() + 5;
					ymax = fontImg.getHeight() + 3;

					for (y = -15; y < 15; y++) {
						for (x = 22 - (xmax - xmin) / 2 + 25 * cur; x < 28 - (xmax - xmin) / 2 + 25 * cur; x++) {
							int z, t, dist;
							dist = 0;
							for (t = 0; t < ymax - ymin; t++) {
								for (z = 0; z < xmax - xmin; z++) {
									int r2;
									if (xmin + z >= fontImg.getWidth() || ymin + t >= fontImg.getHeight()
											|| xmin + z < 0 || ymin + t < 0) {
										r = 255;
									} else {
										r = new Color(fontImg.getRGB(xmin + z, ymin + t)).getGreen();
									}

									if (x + z >= w || y + t >= h || x + z < 0 || y + t < 0) {
										r2 = 255;
									} else {
										r2 = new Color(img.getRGB(x + z, y + t)).getGreen();
									}
									if (r < r2) {
										dist += (r - r2) * (r - r2);
									} else {
										dist += (r - r2) * (r - r2) * 3 / 4;
									}
								}
							}
							if (dist < localmin) {
								localmin = dist;
							}
						}
					}
					if (localmin < bestdist) {
						bestdist = localmin;
						bestch = entry.getKey();
					}
				}
				cur++;
				result.append(fontMap.get(bestch).c);
			}

			System.out.println(result.toString());
		}
	}

}
