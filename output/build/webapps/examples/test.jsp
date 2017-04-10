<html>  
<head>  
<title></title>  
</head>  
<body>  
    <%  
        String mydata = request.getParameter("mydata");  
        if (mydata != null && mydata.length() != 0) {  
            session.setAttribute("mydata", mydata);  
        }  
          
        out.println("request.getLocalAddr(): " + request.getLocalAddr());  
        out.println("<br/>");  
        out.println("request.getLocalPort(): " + request.getLocalPort());  
        out.println("<br/>");  
        out.println("Session ID: " + session.getId());  
        out.println("<br/>");  
          
        out.println("mydata: " + session.getAttribute("mydata"));  
    %>  
    <form>  
        <input type=text size=20 name="mydata">  
        <br>  
        <input type=submit>  
    </form>  
</body>  
</html>  