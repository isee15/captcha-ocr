package cn.z;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import cn.z.util.FontGlyphs;
import cn.z.util.ImageUtil;

public class Pwntcha_Linuxfr {

	public static void main(String[] args) throws Exception {

		final String fontFile = "img/linuxfr/font.png";
		final HashMap<BufferedImage, FontGlyphs> fontMap = ImageUtil.loadFontFixed(fontFile,
				"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");

		for (int k = 0; k < 100; k++) {
			final StringBuilder result = new StringBuilder();
			final String picFile = String.format("img/linuxfr/linuxfr_%03d.png", k);
			BufferedImage img = ImageIO.read(new File(picFile));
			img = ImageUtil.filterThreshold(img, 150);

			int x, y;
			int r;
			int i, j, c;
			final int w = img.getWidth();
			final int h = img.getHeight();
			final int[] stats = new int[h];
			for (y = 0; y < h; y++) {
				int count = 0;
				for (x = 0; x < w; x++) {
					r = new Color(img.getRGB(x, y)).getRed();
					if (r == 0) {
						count++;
					}
				}
				stats[y] = count;
			}
			/*
			 * Find 7 consecutive lines that have at least 14 pixels; they're
			 * baseline candidates
			 */
			for (y = 0; y < h - 11; y++) {
				int ycan = 1;
				for (j = 3; j < 10; j++) {
					if (stats[y + j] < 14) {
						ycan = 0;
						y = y + j - 3;
						break;
					}
				}
				if (ycan == 0) {
					continue;
				}

				/*
				 * Find 7 consecutive cells that have at least 2 pixels on each
				 * line; they're base column candidates
				 */
				for (x = 0; x < w - 9 * 7 + 1; x++) {
					int goodx = 1;
					for (c = 0; c < 7 && goodx == 1; c++) {
						for (j = 3; j < 10; j++) {
							int count = 0;
							for (i = 0; i < 8; i++) {
								r = new Color(img.getRGB(x + c * 9 + i, y + j)).getRed();
								if (r == 0) {
									count++;
									if (count == 2) {
										break;
									}
								}
							}
							if (count < 2) {
								goodx = 0;
								break;
							}
						}
					}
					if (goodx == 0) {
						continue;
					}

					/*
					 * Now we have an (x,y) candidate - try to fit 7 characters
					 */
					for (c = 0; c < 7 && goodx == 1; c++) {
						int r2;
						int minerror = Integer.MAX_VALUE;
						for (final Entry<BufferedImage, FontGlyphs> entry : fontMap.entrySet()) {
							int error = 0, goodch = 1;
							final BufferedImage fontImg = entry.getKey();
							for (j = 0; j < 12 && goodch == 1; j++) {
								for (i = 0; i < 8; i++) {
									r = new Color(img.getRGB(x + c * 9 + i, y + j)).getRed();
									r2 = new Color(fontImg.getRGB(i, j)).getRed();
									/*
									 * Only die if font is black and image is
									 * white
									 */
									if (r > r2) {
										goodch = 0;
										break;
									} else if (r < r2) {
										error++;
									}
								}
							}
							if (goodch == 1 && error < minerror) {
								minerror = error;
								result.append(entry.getValue().c);
							}
						}
						if (minerror == Integer.MAX_VALUE) {
							goodx = 0;
						}
					}
					/* Wow, that was a good guess! Exit this loop */
					if (goodx == 1) {
						break;
					}
				}
			}
			System.out.println(result.toString());
		}
	}

}
