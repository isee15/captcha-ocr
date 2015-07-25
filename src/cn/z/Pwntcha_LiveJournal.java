package cn.z;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import cn.z.util.FontGlyphs;
import cn.z.util.ImageUtil;

public class Pwntcha_LiveJournal {

	public static void main(String[] args) throws Exception {

		final String fontFile = "img/livejournal/makefont.png";
		final HashMap<BufferedImage, FontGlyphs> fontMap = ImageUtil.loadFontVariable(fontFile,
				"0123456789abcdefghijklmnopqrstuvwxyz");

		for (int k = 0; k < 100; k++) {
			final StringBuilder result = new StringBuilder();
			final String picFile = String.format("img/livejournal/livejournal_%03d.png", k);
			BufferedImage img = ImageIO.read(new File(picFile));
			img = ImageUtil.filterDetectLines(img);
			img = ImageUtil.filterFillHoles(img);
			img = ImageUtil.filterMedian(img);
			img = ImageUtil.filterThreshold(img, 128);

			final int w = img.getWidth();
			final int h = img.getHeight();
			int x, y;
			int g, r;
			int xmin, xmax, ymin, ymax, cur = 0;
			int distmin;
			BufferedImage distch = null;

			for (y = 0; y < h; y++) {
				for (x = 0; x < w; x++) {
					g = new Color(img.getRGB(x, y)).getGreen();
					img.setRGB(x, y, new Color(255, g, 255).getRGB());
				}
			}
			while (cur < 7) {
				distmin = Integer.MAX_VALUE;
				for (final Entry<BufferedImage, FontGlyphs> entry : fontMap.entrySet()) {
					int sqr;
					int localmin = Integer.MAX_VALUE;
					final BufferedImage fontImg = entry.getKey();
					xmin = 0;
					ymin = 0;
					xmax = fontImg.getWidth();
					ymax = fontImg.getHeight();
					sqr = (int) Math.sqrt(xmax - xmin);
					for (y = -16; y < 8; y++) {
						for (x = 25 * cur; x < 25 * cur + 5; x++) {
							int z, t, dist;
							dist = 0;
							for (t = 0; t < ymax - ymin; t++) {
								for (z = 0; z < xmax - xmin; z++) {
									int r2;
									r = new Color(fontImg.getRGB(xmin + z, ymin + t)).getGreen();
									if (x + z >= w || y + t >= h || x + z < 0 || y + t < 0) {
										r2 = 255;
									} else {
										r2 = new Color(img.getRGB(x + z, y + t)).getGreen();
									}

									dist += Math.abs(r - r2);
								}
							}
							dist = dist / (xmax - xmin) / sqr;
							if (dist < localmin) {
								localmin = dist;
							}
						}
					}
					if (localmin < distmin) {
						distmin = localmin;
						distch = entry.getKey();
					}
				}
				cur++;
				result.append(fontMap.get(distch).c);
			}
			System.out.println(result.toString());
		}
	}

}
