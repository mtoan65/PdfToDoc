package controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import model.bean.DataAccessSupport;
import model.bo.*;

@WebServlet("/uploadToDB")
@MultipartConfig(	fileSizeThreshold = 1024 * 1024 * 2, // 2MB
       				maxFileSize = 1024 * 1024 * 10, // 10MB
       				maxRequestSize = 1024 * 1024 * 50) // 50MB

public class UploadToDBServlet extends HttpServlet 
{
   private static final long serialVersionUID = 1L;

   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException 
   {
       RequestDispatcher dispatcher = request.getServletContext().getRequestDispatcher("/clientForm.jsp");
       dispatcher.forward(request, response);
   }

   @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
   {
	   DataAccessSupport das = new DataAccessSupport();
	   ConvertProcessBO CPBO = new ConvertProcessBO();
	   try 
	   {
           // Ket noi JDBC
    	   das.LoadJDBC();
    	   das.conn.setAutoCommit(false);
//           conn = ConnectionUtils.getMyConnection();
//           conn.setAutoCommit(false);

           // Duyet qua file upload
           for (Part part : request.getParts()) 
           {
               String fileName = CPBO.extractFileName(part);
               if (fileName != null && fileName.length() > 0) 
               {
                   // Get du lieu noi dung file.
                   InputStream is = part.getInputStream();
                   
                // Chuyen doi pdf -> doc & ghi du lieu noi dung vao database
                   CPBO.ConvertProcess(fileName, is);
               }
           }
           das.conn.commit();
           
           // Tra ve response de thong bao upload thanh cong.
           response.sendRedirect(request.getContextPath() + "/clientForm.jsp");
       } 
	   catch (Exception e) 
	   {
           e.printStackTrace();
           request.setAttribute("errorMessage", "Error: " + e.getMessage());
           RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/clientForm.jsp");
           dispatcher.forward(request, response);
       } 
	   finally 
	   {
           das.CloseConnection();
       }
   } 
}
