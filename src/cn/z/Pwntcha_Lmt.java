package cn.z;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import cn.z.util.FontGlyphs;
import cn.z.util.ImageUtil;

public class Pwntcha_Lmt {

	public static void main(String[] args) throws Exception {

		final String fontFile = "img/lmt/freesans_24_09AZ.bmp";
		final HashMap<BufferedImage, FontGlyphs> fontMap = ImageUtil.loadFontVariable(fontFile,
				"0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");

		for (int k = 0; k < 100; k++) {
			final StringBuilder result = new StringBuilder();
			final String picFile = String.format("img/lmt/lmt_%03d.png", k);
			BufferedImage img = ImageIO.read(new File(picFile));
			img = ImageUtil.filterContrast(img);
			img = ImageUtil.filterBlackStuff(img);
			img = ImageUtil.filterSmooth(img);
			img = ImageUtil.filterMedian(img);

			final int DELTA = 2;
			final int w = img.getWidth();
			final int h = img.getHeight();

			int cur = 0, x, y, r;
			int startx = 0;
			int bestdist, bestx = 0;
			int xmin, ymin, xmax, ymax;
			BufferedImage bestch = null;
			while (cur < 3) {

				bestdist = Integer.MAX_VALUE;
				for (final Entry<BufferedImage, FontGlyphs> entry : fontMap.entrySet()) {

					int localmin = Integer.MAX_VALUE;
					int localx = 0;
					final BufferedImage fontImg = entry.getKey();
					xmin = -DELTA;
					ymin = 0;
					xmax = fontImg.getWidth() + DELTA;
					ymax = fontImg.getHeight();
					for (y = -5; y < 5; y++) {
						for (x = startx; x < startx + 15; x++) {
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

									dist += (r - r2) * (r - r2);
								}
							}
							dist = dist / (xmax - xmin - 2 * DELTA);
							if (dist < localmin) {
								localmin = dist;
								localx = x;
							}
						}
					}
					if (localmin < bestdist) {
						bestdist = localmin;
						bestx = localx;
						bestch = entry.getKey();
					}
				}
				cur++;
				startx = bestx + bestch.getWidth();
				result.append(fontMap.get(bestch).c);
			}
			System.out.println(result.toString());
		}
	}

}
