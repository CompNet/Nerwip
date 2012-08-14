<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html><!-- InstanceBegin template="/Templates/software_demos.dwt.php" codeOutsideHTMLIsLocked="false" -->
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<!-- InstanceBeginEditable name="doctitle" -->
<title>CCG: Lbj-Based Named Entity Tagger</title>
<!-- InstanceEndEditable --><!-- InstanceBeginEditable name="head" --><!-- InstanceEndEditable -->
<link href="ccg.css" rel="stylesheet" type="text/css">
<?php
	$php_include = "/home/roth/cogcomp/public_html";
	include "$php_include/func.php";
	include "$php_include/menu.php";
?>
<script language="JavaScript" type="text/JavaScript">
<!--
function MM_reloadPage(init) {  //reloads the window if Nav4 resized
  if (init==true) with (navigator) {if ((appName=="Netscape")&&(parseInt(appVersion)==4)) {
    document.MM_pgW=innerWidth; document.MM_pgH=innerHeight; onresize=MM_reloadPage; }}
  else if (innerWidth!=document.MM_pgW || innerHeight!=document.MM_pgH) location.reload();
}
MM_reloadPage(true);
//-->
</script>
<script language="JavaScript1.2" src="mm_menu.js"></script>
</head>

<body>
<script language="JavaScript1.2">mmLoadMenus();</script>
<div id="Header"><div id="MainMenuBackground"><a name="Top"><img src="images/CCG_header.gif" width="750" height="68" border="0" usemap="#Map"></a></div>
    <div id="MainMenu"><?php main_menu(); ?></div>
</div>
<div id="Content">
	
	<div id="LeftImage"> <img src="images/flower.jpg"></div>
	<div id="Section">
		<div id="SectionHeader">
			<div id="SectionMenuBackground"> <img src="images/software_demos_header.gif" width="666" height="31"></div>
			<div id="SectionMenu"> <?php software_demos_menu(); ?></div>
		</div>
		<div id="ContentBody">
			<!-- InstanceBeginEditable name="ContentBody" -->
			<h3>Lbj-Based Named Entity Tagger</h3>
        <FORM name="myform" METHOD="POST" ACTION="http://l2r.cs.uiuc.edu/cgi-bin/LbjNer-front.pl">
          Please enter your text here (Note that empty line indicates end of input):<br><br>
          <table>
            <tr>
              <td colspan=2>
                <!-- <input NAME="sentence" size=80><br><br> -->
                <INPUT TYPE=hidden name="dest" value="NETagger">
                <textarea valign=top NAME="sentence" rows=20 cols=60 wrap=virtual></textarea><br><br>
                <INPUT TYPE="hidden" NAME=".cgifields" VALUE="dest">
               </td>
            </tr>
            <tr>
              <td>
                <input type=checkbox name="forceNewlines" value="true" checked> Newlines indicate sentence breaks (recommended for webpages)<br>
              </td>
	    </tr>
	    <tr>
              <td align=left>
                <INPUT TYPE="submit" NAME="Process" VALUE="Process">
                <INPUT TYPE="reset" VALUE="Clear">
              </td>
            </tr>
          </table>
        </FORM>
        <!-- InstanceEndEditable -->
		</div>
	</div>
  
</div>
<div id="Footer">Copyright &copy; <?php echo $copyright_year; ?>, <a href="http://www.uiuc.edu">University of Illinois at Urbana-Champaign</a>, All Rights Reserved.</div>
<map name="Map">
<area shape="rect" coords="650,1,749,67" href="index.php">
</map>
</body>
<!-- InstanceEnd --></html>
