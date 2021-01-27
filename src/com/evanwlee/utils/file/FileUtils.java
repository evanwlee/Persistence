package com.evanwlee.utils.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.security.DigestInputStream;
import java.security.MessageDigest;

import org.apache.log4j.Logger;

import com.evanwlee.utils.logging.LoggerFactory;

public class FileUtils {
	private static Logger log = LoggerFactory.getLogger(FileUtils.class.getName());

    /** The size of blocking to use */
    protected static final int BLKSIZ = 8192;
    
    public static boolean fileExists(String path){
    	File varTmpDir = new File(path);
    	return varTmpDir.exists();
    }
    
    public static boolean delete(String path){
    	if(fileExists(path)){
	    	File varTmpDir = new File(path);
	    	return varTmpDir.delete();
    	}
    	
    	return true;
    }

	/**
	 * Returns the content of a file as a String
	 * @param filePath the name of the file to open. Not sure if it can accept URLs
	 *            or just filenames. Path handling could be better, and buffer
	 *            sizes are hardcoded
	 *            
	 * @return string content of the file.
	 */
	public static String readFileAsString(String filePath) throws IOException {
		String result = "";
		BufferedReader reader = null;
		try{
			StringBuffer fileData = new StringBuffer(1024);
			reader = new BufferedReader(new FileReader(filePath));
			char[] buf = new char[1024];
			int numRead = 0;
			while ((numRead = reader.read(buf)) != -1) {
				fileData.append(buf, 0, numRead);
			}
			reader.close();
			result = fileData.toString();
		}catch(IOException e){
			throw e;
		}finally{
			if( reader != null){
				reader.close();
			}
		}
		
		return result;
	}
	
	/**
	 * Writes content to a file
	 * @param content string to write to a file
	 * @param filePath file to write to          
	 */
	public static void writeTextFile(String contents, String fullPathFilename) throws IOException{
		BufferedWriter writer = null;
		try{
			writer = new BufferedWriter(new FileWriter(fullPathFilename));
			writer.write(contents);
			writer.flush();
			writer.close();	
		}catch(IOException e){
			throw e;
		}finally{
			if( writer != null){
				writer.close();
			}
		}
	}
	
	 public static void copy(File source, File destination) throws IOException{
		 	
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(source));
		        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destination));
			    int numRead;                
		        byte[] bytes = new byte[1024];
		        while ((numRead = bis.read(bytes)) != -1) {
		            bos.write(bytes,0,numRead);
		        }
		        
		        try{
		        	bis.close();
		        } catch (Exception e){}
		        	
		        try{
		            bos.close();
		        } catch (Exception e){}
		   }

	    /** Copy a file from one filename to another */
	    public static void copyFile(String inName, String outName)
	    throws FileNotFoundException, IOException {
	        BufferedInputStream is =
	        new BufferedInputStream(new FileInputStream(inName));
	        
	        java.io.BufferedOutputStream os =
	        new java.io.BufferedOutputStream(new FileOutputStream(outName));
	        copyFile(is, os, true);
	    }
	    
	    /** Copy a file from an opened InputStream to opened OutputStream */
	    public static void copyFile(InputStream is, java.io.OutputStream os, boolean close) throws IOException {
	        int b;				// the byte read from the file
	        while ((b = is.read()) != -1) {
	            os.write(b);
	        }
	        is.close();
	        if (close)
	            os.close();
	    }
	    
	    /** Copy a file from an opened Reader to opened Writer */
	    public static void copyFile(Reader is, java.io.Writer os, boolean close) throws IOException {
	        int b;				// the byte read from the file
	        while ((b = is.read()) != -1) {
	            os.write(b);
	        }
	        is.close();
	        if (close)
	            os.close();
	    }
	    
	    /** Copy a file from a filename to a PrintWriter. */
	    public static void copyFile(String inName, java.io.PrintWriter pw, boolean close) throws FileNotFoundException, IOException {
	        BufferedReader is = new BufferedReader(new FileReader(inName));
	        copyFile(is, pw, close);
	    }
	    
	    /**
	     * Moves the file named by srcPath to the location named by destPath.
	     *
	     * @param srcPath
	     * @param destPath
	     *
	     * @return true if and only if the move succeeded; false otherwise
	     */
	    public static boolean moveFile(String srcPath, String destPath) {
	      File srcFile  = new File(srcPath);
	      File destFile = new File(destPath);

	      return srcFile.renameTo(destFile);
	    }
	    
	    /** Read the entire content of a Reader into a String */
	    public static String readerToString(Reader is) throws IOException {
	        StringBuffer sb = new StringBuffer();
	        char[] b = new char[BLKSIZ];
	        int n;
	        
	        // Read a block. If it gets any chars, append them.
	        while ((n = is.read(b)) > 0) {
	            sb.append(b, 0, n);
	        }
	        
	        // Only construct the String object once, here.
	        return sb.toString();
	    }
	    /**
	     * Get the file name from the full path name.
	     *
	     * @param fileName Full path name of the file.
	     *
	     * @return File name
	     */
	    public static String getFileName(String fileName) {
	      String[] seperatedText = fileName.split("\\\\");
	      String   file = seperatedText[seperatedText.length - 1];

	      return file;
	    }

	    /**
	     * Reads byes from the input stream and returns a byte array with the
	     * contents.
	     *
	     * @param input InputStream object
	     * @return byte[] Byte array of the input stream
	     * @throws IOException
	     */
	    public static byte[] getFileBytes(InputStream input) throws IOException {
	      ByteArrayOutputStream out    = new ByteArrayOutputStream();
	      int                   read;
	      final byte[]          buffer = new byte[1024 * 4];

	      while ((read = input.read(buffer)) != -1) {
	        out.write(buffer, 0, read);
	      }

	      return out.toByteArray();
	    }
	    
		
		/**
		 * Maker sure the directory exist and is writable
		 * @param dir
		 * @return
		 */
		public static String setupFileSystem(String dir){
			String reportDir = dir;

			//Make directory if it doesn't exist
			File theDir = new File(reportDir);
			if (!theDir.exists()) theDir.mkdir();
			theDir.setExecutable(true);
			theDir.setReadable(true);
			theDir.setWritable(true);
			
			return reportDir;
		}
		
	    /**
	     * 
	     * Caller should close the stream
	     * 
	     * @param fis
	     * @return md5 checksum for file
	     */
	    public static String computeContentMD5Value( FileInputStream fis ) {
	    	try{
	    	    DigestInputStream dis = new DigestInputStream( fis,
	    	        MessageDigest.getInstance( "MD5" ));

	    	    byte[] buffer = new byte[8192];
	    	    while( dis.read( buffer ) > 0 );

	    	    String md5Content = new String(
	    	        org.apache.commons.codec.binary.Base64.encodeBase64(
	    	            dis.getMessageDigest().digest()) ); 

	    	    // Effectively resets the stream to be beginning of the file
	    	    // via a FileChannel.
	    	    fis.getChannel().position( 0 );
	    	    return md5Content;
	    	}catch(Exception e){
	    		log.error("Problem in computeContentMD5Value" + e.getMessage());
	    	}
	    	return "";
	    	    
	    }
	    
	    /**
	     * Method opens stream and calculates then closes.
	     * @param path to a file
	     * @return md5 checksum for file
	     */
	    public static String computeContentMD5Value( String path ) {
	    	DigestInputStream dis = null;
	    	try{
	    	    dis = new DigestInputStream( new FileInputStream(new File(path)),
	    	        MessageDigest.getInstance( "MD5" ));

	    	    byte[] buffer = new byte[8192];
	    	    while( dis.read( buffer ) > 0 );

	    	    String md5Content = new String(
	    	        org.apache.commons.codec.binary.Base64.encodeBase64(
	    	            dis.getMessageDigest().digest()) ); 

	    	    // Effectively resets the stream to be beginning of the file
	    	    // via a FileChannel.
	    	    return md5Content;
	    	}catch(Exception e){
	    		log.error("Problem in computeContentMD5Value" + e.getMessage());
	    	}finally{
	    		if(dis != null)try{ dis.close();}catch(Exception e){}
	    	}
	    	return "";
	    	    
	    }
		
}
