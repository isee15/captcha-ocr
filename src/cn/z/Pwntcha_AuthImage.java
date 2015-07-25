package cn.z;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import cn.z.util.FontGlyphs;
import cn.z.util.ImageUtil;

public class Pwntcha_AuthImage {

	public static void main(String[] args) throws Exception {

		final String fontFile = "img/authimage/font.png";
		final HashMap<BufferedImage, FontGlyphs> fontMap = ImageUtil.loadFontFixed(fontFile,
				"0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		for (int k = 0; k < 100; k++) {
			final String picFile = String.format("img/authimage/authimage_%03d.jpeg", k);
			BufferedImage img = ImageIO.read(new File(picFile));
			img = ImageUtil.filterScale(img, 2.0f);
			final Color color = new Color(img.getRGB(0, 0));
			img = ImageUtil.filterThreshold(img, color.getRed() * 3 / 4);
			img = ImageUtil.filterSmooth(img);
			img = ImageUtil.filterThreshold(img, 220);

			int x, y;
			int r, r2;
			final StringBuilder result = new StringBuilder();
			for (int i = 0; i < 6; i++) {
				int mindiff = Integer.MAX_VALUE;
				BufferedImage minch = null;
				for (final Entry<BufferedImage, FontGlyphs> entry : fontMap.entrySet()) {
					int diff = 0;
					for (y = 0; y < 7; y++) {
						for (x = 0; x < 5; x++) {
							int newx, newy;
							newx = (int) (35.0 + (x + 6 * i) * 218.0 / 34.0 + y * 5.0 / 6.0 + 0.5);
							newy = (int) (33.0 - (x + 6 * i) * 18.0 / 34.0 + y * 42.0 / 6.0 + 0.5);
							r = new Color(img.getRGB(newx, newy)).getRed();
							r2 = new Color(entry.getKey().getRGB(x, y)).getRed();
							diff += (r - r2) * (r - r2);
						}
					}
					if (diff < mindiff) {
						mindiff = diff;
						minch = entry.getKey();
					}
				}
				result.append(fontMap.get(minch).c);
			}
			System.out.println(result.toString());
		}
	}

}
