package cn.z;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import cn.z.util.FontGlyphs;
import cn.z.util.ImageUtil;

public class Pwntcha_Phpbb {

	public static void main(String[] args) throws Exception {

		final String fontFile = "img/phpbb/font.png";
		final HashMap<BufferedImage, FontGlyphs> fontMap = ImageUtil.loadFontFixed(fontFile,
				"ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789");
		for (int k = 0; k < 100; k++) {
			final StringBuilder result = new StringBuilder();
			final String picFile = String.format("img/phpbb/phpbb_%03d.png", k);
			BufferedImage img = ImageIO.read(new File(picFile));
			img = ImageUtil.filterSmooth(img);
			// ImageIO.write(img, "BMP", new File("phpbb11.png"));
			img = ImageUtil.filterThreshold(img, 200);

			final BufferedImage oriImg = ImageIO.read(new File(picFile));
			int x, y;
			int r;
			int xmin = 0, xmax = 0, ymin, ymax, cur, offset = -1;
			int distmin, distx = 0;
			BufferedImage distch = null;
			final int w = img.getWidth();
			final int h = img.getHeight();
			final BufferedImage newImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

			for (x = 0; x < w; x++) {
				for (y = 0; y < h; y++) {
					Color rgb = new Color(img.getRGB(x, y));

					if (rgb.getRed() == 0 && offset == -1) {
						offset = x;
					}
					rgb = new Color(oriImg.getRGB(x, y));
					newImg.setRGB(x, y, new Color(255, rgb.getGreen(), 255).getRGB());
				}
			}
			for (cur = 0; cur < 6; cur++) {
				/* Try to find 1st letter */
				distmin = Integer.MAX_VALUE;
				for (final Entry<BufferedImage, FontGlyphs> entry : fontMap.entrySet()) {
					int localmin = Integer.MAX_VALUE, localx = 0;
					final BufferedImage fontImg = entry.getKey();
					xmin = 0;
					ymin = 0;
					xmax = fontImg.getWidth();
					ymax = fontImg.getHeight();
					for (y = 0; y < h - (ymax - ymin); y++) {
						x = offset - 3;
						if (cur == 0) {
							x -= 10;
						}
						if (x < 0) {
							x = 0;
						}
						for (; x < offset + 3; x++) {
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
									if (r > r2) {
										dist += r - r2;
									} else {
										dist += (r2 - r) * 3 / 4;
									}
								}
							}
							if (dist < localmin) {
								localmin = dist;
								localx = x;
							}
						}
					}
					if (localmin < distmin) {
						distmin = localmin;
						distx = localx;
						distch = entry.getKey();
					}
				}

				offset = distx + xmax - xmin;
				result.append(fontMap.get(distch).c);
			}
			System.out.println(result.toString());
		}
	}

}
