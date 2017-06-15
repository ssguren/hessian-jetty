package base.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.net.URL;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

public class FileUtil {

	private static final Logger log = Logger.getLogger(FileUtil.class);

	private static final String CHARSET = "UTF-8";

	public static File getFile(String path) {
		String applicationPath = FileUtil.class.getClassLoader()
				.getResource("").getPath();
		return new File(applicationPath, path);
	}

	public static ByteBuffer File2ByteBuffer(String path) {
		return File2ByteBuffer(new File(path));
	}

	public static ByteBuffer File2ByteBuffer(File file) {
		RandomAccessFile raf = null;
		try {
			if (file == null || !file.isFile() || !file.exists()
					|| !file.canRead()) {
			} else {
				raf = new RandomAccessFile(file, "r");
				if (raf.length() > Integer.MAX_VALUE) {
				} else {
					byte[] content = new byte[(int) raf.length()];
					raf.read(content);
					ByteBuffer buffer = ByteBuffer.wrap(content);

					return buffer;
				}
			}
		} catch (IOException e) {
			log.error(MiscUtil.traceInfo(e));
		} finally {
			try {
				if (raf != null)
					raf.close();
			} catch (IOException e) {
				log.error(MiscUtil.traceInfo(e));
			}
		}

		return null;
	}

	public static File ByteBuffer2File(String path, ByteBuffer buffer) {
		return ByteBuffer2File(new File(path), buffer, true);
	}

	public static File ByteBuffer2File(String path, ByteBuffer buffer,
			boolean append) {
		return ByteBuffer2File(new File(path), buffer, append);
	}

	public static File ByteBuffer2File(File file, ByteBuffer buffer) {
		return ByteBuffer2File(file, buffer, true);
	}

	public static File ByteBuffer2File(File file, ByteBuffer buffer,
			boolean append) {
		RandomAccessFile raf = null;
		try {
			if (file != null && buffer != null) {
				if (file.exists() && file.isFile()) {
					raf = new RandomAccessFile(file, "rwd");

					if (append) {
						raf.seek(raf.length());
					} else {
						raf.seek(0);
						raf.setLength(buffer.limit());
					}

					raf.write(buffer.array());
				} else {
					file.createNewFile();

					raf = new RandomAccessFile(file, "rwd");
					raf.setLength(buffer.limit());
					raf.write(buffer.array());
				}

				return file;
			}
		} catch (IOException e) {
			log.error(MiscUtil.traceInfo(e));
		} finally {
			try {
				if (raf != null)
					raf.close();
			} catch (IOException e) {
				log.error(MiscUtil.traceInfo(e));
			}
		}

		return null;
	}

	public static String TXTFile2String(String path, String charsetName) {
		return TXTFile2String(new File(path), charsetName);
	}

	public static String TXTFile2String(File file, String charsetName) {
		String res = null;
		InputStreamReader isr = null;
		StringWriter out = null;

		try {
			if (file != null && file.isFile() && file.canRead()) {
				isr = new InputStreamReader(new FileInputStream(file),
						charsetName == null ? CHARSET : charsetName);
				out = new StringWriter();

				int offset = -1;
				char[] cbuf = new char[1024];
				while ((offset = isr.read(cbuf)) != -1) {
					out.write(cbuf, 0, offset);
				}

				out.flush();
				res = out.toString();
			}
		} catch (IOException e) {
			log.error(MiscUtil.traceInfo(e));
		} finally {
			try {
				if (isr != null)
					isr.close();
			} catch (IOException e) {
				log.error(e.getMessage());
			}
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {
				log.error(e.getMessage());
			}
		}

		return res;
	}

	public static File String2TXTFile(String path, String content,
			String charsetName) {
		return String2TXTFile(new File(path), content, true, charsetName);
	}

	public static File String2TXTFile(String path, String content,
			boolean append, String charsetName) {
		return String2TXTFile(new File(path), content, append, charsetName);
	}

	public static File String2TXTFile(File file, String content,
			String charsetName) {
		return String2TXTFile(file, content, true, charsetName);
	}

	public static File String2TXTFile(File file, String content,
			boolean append, String charsetName) {
		OutputStreamWriter osw = null;

		try {
			if (file != null && content != null) {
				if (file.exists() && file.isFile()) {
					osw = new OutputStreamWriter(new FileOutputStream(file,
							append), charsetName == null ? CHARSET
							: charsetName);

					osw.write(content);
				} else {
					file.createNewFile();

					osw = new OutputStreamWriter(new FileOutputStream(file),
							charsetName == null ? CHARSET : charsetName);

					osw.write(content);
				}

				return file;
			}
		} catch (IOException e) {
			log.error(MiscUtil.traceInfo(e));
		} finally {
			try {
				if (osw != null)
					osw.close();
			} catch (IOException e) {
				log.error(MiscUtil.traceInfo(e));
			}
		}

		return null;
	}

	public static boolean copyFile(File source, File target) {
		InputStream in = null;
		OutputStream out = null;

		try {
			if (!target.getParentFile().exists())
				target.getParentFile().mkdirs();
			if (!target.exists())
				target.createNewFile();

			in = new BufferedInputStream(new FileInputStream(source));
			out = new BufferedOutputStream(new FileOutputStream(target));
			byte[] tmp = new byte[(int) source.length()];
			int read = 0;

			while ((read = in.read(tmp)) > 0) {
				out.write(tmp, 0, read);
			}

			return true;
		} catch (IOException e) {
			log.error(MiscUtil.traceInfo(e));
			return false;
		} finally {
			try {
				if (in != null)
					in.close();
				if (out != null)
					out.close();
			} catch (IOException e) {
				in = null;
				out = null;
				log.error(MiscUtil.traceInfo(e));
			}
		}
	}

	public static String getRelativePath(File parent, File child) {
		String parentPath = parent.getAbsolutePath();
		String childPath = child.getAbsolutePath();

		int idx = childPath.indexOf(parentPath);
		if (idx != -1) {
			return childPath.substring(parentPath.length());
		} else {
			return child.getAbsolutePath();
		}
	}

	public static URL getFileURLInCP(String fileName) {
		try {
			return FileUtil.class.getClassLoader().getResource(fileName);
		} catch (Exception e) {
			log.error(MiscUtil.traceInfo(e));
		}

		return null;
	}

	public static InputStream getFileStreamInCP(String fileName) {
		try {
			return FileUtil.class.getClassLoader()
					.getResourceAsStream(fileName);
		} catch (Exception e) {
			log.error(MiscUtil.traceInfo(e));
		}

		return null;
	}

	public static boolean isFileExist(String path) {
		if (path == null)
			return false;
		else
			return isFileExist(new File(path));
	}

	public static boolean isFileExist(File file) {
		return (file != null && file.exists());
	}

}
