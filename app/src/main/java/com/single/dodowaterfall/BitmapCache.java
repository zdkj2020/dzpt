package com.single.dodowaterfall;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Hashtable;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
/**
 * 闃叉婧㈠嚭
 *
 */
public class BitmapCache {
	static private BitmapCache cache;
	/** 鐢ㄤ簬Chche鍐呭鐨勫瓨鍌� */
	private Hashtable<String, BtimapRef> bitmapRefs;
	/** 鍨冨溇Reference鐨勯槦鍒楋紙鎵�寮曠敤鐨勫璞″凡缁忚鍥炴敹锛屽垯灏嗚寮曠敤瀛樺叆闃熷垪涓級 */
	private ReferenceQueue<Bitmap> q;

	/**
	 * 缁ф壙SoftReference锛屼娇寰楁瘡涓�涓疄渚嬮兘鍏锋湁鍙瘑鍒殑鏍囪瘑銆�
	 */
	private class BtimapRef extends SoftReference<Bitmap> {
		private String _key = "";

		public BtimapRef(Bitmap bmp, ReferenceQueue<Bitmap> q, String key) {
			super(bmp, q);
			_key = key;
		}
	}

	private BitmapCache() {
		bitmapRefs = new Hashtable<String, BtimapRef>();
		q = new ReferenceQueue<Bitmap>();

	}

	/**
	 * 鍙栧緱缂撳瓨鍣ㄥ疄渚�
	 */
	public static BitmapCache getInstance() {
		if (cache == null) {
			cache = new BitmapCache();
		}
		return cache;

	}

	/**
	 * 浠ヨ蒋寮曠敤鐨勬柟寮忓涓�涓狟itmap瀵硅薄鐨勫疄渚嬭繘琛屽紩鐢ㄥ苟淇濆瓨璇ュ紩鐢�
	 */
	private void addCacheBitmap(Bitmap bmp, String key) {
		cleanCache();// 娓呴櫎鍨冨溇寮曠敤
		BtimapRef ref = new BtimapRef(bmp, q, key);
		bitmapRefs.put(key, ref);
	}

	/**
	 * 渚濇嵁鎵�鎸囧畾鐨勬枃浠跺悕鑾峰彇鍥剧墖
	 */
	public Bitmap getBitmap(String filename, AssetManager assetManager) {

		Bitmap bitmapImage = null;
		// 缂撳瓨涓槸鍚︽湁璇itmap瀹炰緥鐨勮蒋寮曠敤锛屽鏋滄湁锛屼粠杞紩鐢ㄤ腑鍙栧緱銆�
		if (bitmapRefs.containsKey(filename)) {
			BtimapRef ref = (BtimapRef) bitmapRefs.get(filename);
			bitmapImage = (Bitmap) ref.get();
		}
		// 濡傛灉娌℃湁杞紩鐢紝鎴栬�呬粠杞紩鐢ㄤ腑寰楀埌鐨勫疄渚嬫槸null锛岄噸鏂版瀯寤轰竴涓疄渚嬶紝
		// 骞朵繚瀛樺杩欎釜鏂板缓瀹炰緥鐨勮蒋寮曠敤
		if (bitmapImage == null) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inTempStorage = new byte[16 * 1024];

			// bitmapImage = BitmapFactory.decodeFile(filename, options);
			BufferedInputStream buf;
			try {
				buf = new BufferedInputStream(assetManager.open(filename));
				bitmapImage = BitmapFactory.decodeStream(buf);
				this.addCacheBitmap(bitmapImage, filename);
			} catch (IOException e) {

				e.printStackTrace();
			}

		}

		return bitmapImage;
	}

	private void cleanCache() {
		BtimapRef ref = null;
		while ((ref = (BtimapRef) q.poll()) != null) {
			bitmapRefs.remove(ref._key);
		}
	}

	// 娓呴櫎Cache鍐呯殑鍏ㄩ儴鍐呭
	public void clearCache() {
		cleanCache();
		bitmapRefs.clear();
		System.gc();
		System.runFinalization();
	}

}
