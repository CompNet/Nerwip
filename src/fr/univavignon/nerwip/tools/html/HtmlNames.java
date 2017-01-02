package fr.univavignon.nerwip.tools.html;

/*
 * Nerwip - Named Entity Extraction in Wikipedia Pages
 * Copyright 2011-17 Vincent Labatut et al.
 * 
 * This file is part of Nerwip - Named Entity Extraction in Wikipedia Pages.
 * 
 * Nerwip - Named Entity Extraction in Wikipedia Pages is free software: you can 
 * redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Nerwip - Named Entity Extraction in Wikipedia Pages is distributed in the hope 
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public 
 * License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Nerwip - Named Entity Extraction in Wikipedia Pages.  
 * If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * This class contains HTML-related strings.
 * 
 * @author Vincent Labatut
 */
public class HtmlNames
{	
	/////////////////////////////////////////////////////////////////
	// HTML ATTRIBUTES	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Html id attribute */
	public final static String ATT_ID = "id";
	/** Html class attribute */
	public final static String ATT_CLASS = "class";
	/** Html datetime attribute */
	public static final String ATT_DATETIME = "datetime";
	/** Html href attribute */
	public final static String ATT_HREF = "href";
	/** Html title attribute */
	public final static String ATT_TITLE = "title";

	/////////////////////////////////////////////////////////////////
	// HTML ELEMENTS		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Html hyperlink tag */
	public final static String ELT_A = "a";
	/** Html abbreviation tag */
	public final static String ELT_ABBR = "abbr";
	/** Html acronym tag (deprecated) */
	public final static String ELT_ACRONYM = "acronym";
	/** Html address tag */
	public final static String ELT_ADDRESS = "address";
	/** Html applet tag (deprecated) */
	public final static String ELT_APPLET = "applet";
	/** Html image zone defintion tag */
	public final static String ELT_AREA = "area";
	/** Html article tag */
	public final static String ELT_ARTICLE = "article";
	/** Html content aside from the main content tag */
	public final static String ELT_ASIDE = "aside";
	/** Html audio tag */
	public final static String ELT_AUDIO = "audio";
	/** Html bold tag */
	public final static String ELT_B = "b";
	/** Html base tag */
	public final static String ELT_BASE = "base";
	/** Html base font tag (deprecated) */
	public final static String ELT_BASEFONT = "basefont";
	/** Html bdi tag */
	public final static String ELT_BDI = "bdi";
	/** Html bdo tag */
	public final static String ELT_BDO = "bdo";
	/** Html big tag (deprecated) */
	public final static String ELT_BIG = "big";
	/** Html blink tag (deprecated) */
	public final static String ELT_BLINK = "blink";
	/** Html offline quote tag */
	public final static String ELT_BLOCKQUOTE = "blockquote";
	/** Html body tag */
	public final static String ELT_BODY = "body";
	/** Html br tag */
	public final static String ELT_BR = "br";
	/** Html button tag */
	public final static String ELT_BUTTON = "button";
	/** Html canvas tag */
	public final static String ELT_CANVAS = "canvas";
	/** Html table caption tag */
	public final static String ELT_CAPTION = "caption";
	/** Html center tag */
	public final static String ELT_CENTER = "center";
	/** Html cite tag */
	public final static String ELT_CITE = "cite";
	/** Html code tag */
	public final static String ELT_CODE = "code";
	/** Html column properties tag */
	public final static String ELT_COL = "col";
	/** Html column group tag */
	public final static String ELT_COLGROUP = "colgroup";
	/** Html Web component content tag */
	public final static String ELT_CONTENT = "content";
	/** Html data tag */
	public final static String ELT_DATA = "data";
	/** Html input options tag */
	public final static String ELT_DATALIST = "datalist";
	/** Html description definition tag */
	public final static String ELT_DD = "dd";
	/** Html Web component decorator tag */
	public final static String ELT_DECORATOR = "decorator";
	/** Html deleted text tag */
	public final static String ELT_DEL = "del";
	/** Html details tag */
	public final static String ELT_DETAILS = "details";
	/** Html definition tag */
	public final static String ELT_DFN = "dfn";
	/** Html dialog tag */
	public final static String ELT_DIALOG = "dialog";
	/** Html directory list tag (deprecated) */
	public final static String ELT_DIR = "dir";
	/** Html division tag */
	public final static String ELT_DIV = "div";
	/** Html description list tag */
	public final static String ELT_DL = "dl";
	/** Html description term tag */
	public final static String ELT_DT = "dt";
	/** Html element tag */
	public final static String ELT_ELEMENT = "element";
	/** Html em tag */
	public final static String ELT_EM = "em";
	/** Html embed application tag */
	public final static String ELT_EMBED = "embed";
	/** Html fieldset tag */
	public final static String ELT_FIELDSET = "fieldset";
	/** Html figure caption tag */
	public final static String ELT_FIGCAPTION = "figcaption";
	/** Html figure tag */
	public final static String ELT_FIGURE = "figure";
	/** Html font tag (deprecated) */
	public final static String ELT_FONT = "font";
	/** Html footer tag */
	public final static String ELT_FOOTER = "footer";
	/** Html form tag */
	public final static String ELT_FORM = "form";
	/** Html frame tag (deprecated) */
	public final static String ELT_FRAME = "frame";
	/** Html frame set tag (deprecated) */
	public final static String ELT_FRAMESET = "frameset";
	/** Html section tag */
	public final static String ELT_H1 = "h1";
	/** Html section tag */
	public final static String ELT_H2 = "h2";
	/** Html section tag */
	public final static String ELT_H3 = "h3";
	/** Html section tag */
	public final static String ELT_H4 = "h4";
	/** Html section tag */
	public final static String ELT_H5 = "h5";
	/** Html section tag */
	public final static String ELT_H6 = "h6";
	/** Html document head tag */
	public final static String ELT_HEAD = "head";
	/** Html section header tag */
	public final static String ELT_HEADER = "header";
	/** Html hgroup tag (deprecated) */
	public final static String ELT_HGROUP = "hgroup";
	/** Html thematic break tag */
	public final static String ELT_HR = "hr";
	/** Html document tag */
	public final static String ELT_HTML = "html";
	/** Html italic tag */
	public final static String ELT_I = "i";
	/** Html inline frame tag */
	public final static String ELT_IFRAME = "iframe";
	/** Html image tag */
	public final static String ELT_IMG = "img";
	/** Html input control tag */
	public final static String ELT_INPUT = "input";
	/** Html inserted text tag */
	public final static String ELT_INS = "ins";
	/** Html input text tag */
	public final static String ELT_ISINDEX = "isindex";
	/** Html keyboard input tag */
	public final static String ELT_KBD = "kbd";
	/** Html keygen form field tag */
	public final static String ELT_KEYGEN = "keygen";
	/** Html input label tag */
	public final static String ELT_LABEL = "label";
	/** Html fieldset legend tag */
	public final static String ELT_LEGEND = "legend";
	/** Html list item tag */
	public final static String ELT_LI = "li";
	/** Html stylesheet link tag */
	public final static String ELT_LINK = "link";
	/** Html listing tag */
	public final static String ELT_LISTING = "listing";
	/** Html main content tag */
	public final static String ELT_MAIN = "main";
	/** Html image map tag */
	public final static String ELT_MAP = "map";
	/** Html marked text tag */
	public final static String ELT_MARK = "mark";
	/** Html menu tag */
	public final static String ELT_MENU = "menu";
	/** Html menu item tag */
	public final static String ELT_MENUITEM = "menuitem";
	/** Html document metadata tag */
	public final static String ELT_META = "meta";
	/** Html form meter tag */
	public final static String ELT_METER = "meter";
	/** Html navigation links tag */
	public final static String ELT_NAV = "nav";
	/** Html no frames alternative tag (deprecated) */
	public final static String ELT_NOFRAMES = "noframes";
	/** Html no embed alternative tag */
	public final static String ELT_NOEMBED = "noembed";
	/** Html no script alternative tag */
	public final static String ELT_NOSCRIPT = "noscript";
	/** Html object tag */
	public final static String ELT_OBJECT = "object";
	/** Html ordered list tag */
	public final static String ELT_OL = "ol";
	/** Html form option group tag */
	public final static String ELT_OPTGROUP = "optgroup";
	/** Html form option tag */
	public final static String ELT_OPTION = "option";
	/** Html output tag */
	public final static String ELT_OUTPUT = "output";
	/** Html paragraph tag */
	public final static String ELT_P = "p";
	/** Html object parameter tag */
	public final static String ELT_PARAM = "param";
	/** Html plaintext tag */
	public final static String ELT_PLAINTEXT = "plaintext";
	/** Html plain text tag */
	public final static String ELT_PRE = "pre";
	/** Html progress tag */
	public final static String ELT_PROGRESS = "progress";
	/** Html short quote tag */
	public final static String ELT_Q = "q";
	/** Html inline quote tag */
	public final static String ELT_QUOTE = "quote";
	/** Html rp tag */
	public final static String ELT_RP = "rp";
	/** Html rt tag */
	public final static String ELT_RT = "rt";
	/** Html rtc tag */
	public final static String ELT_RTC = "rtc";
	/** Html ruby tag */
	public final static String ELT_RUBY = "ruby";
	/** Html strikethrough tag */
	public final static String ELT_S = "s";
	/** Html sample tag */
	public final static String ELT_SAMP = "samp";
	/** Html script tag */
	public final static String ELT_SCRIPT = "script";
	/** Html shadow tag */
	public final static String ELT_SHADOW = "shadow";
	/** Html section tag */
	public final static String ELT_SECTION = "section";
	/** Html select tag */
	public final static String ELT_SELECT = "select";
	/** Html small text tag */
	public final static String ELT_SMALL = "small";
	/** Html audio source tag */
	public final static String ELT_SOURCE = "source";
	/** Html spacer tag */
	public final static String ELT_SPACER = "spacer";
	/** Html span tag */
	public final static String ELT_SPAN = "span";
	/** Html strikethrough tag */
	public final static String ELT_STRIKE = "strike";
	/** Html strong tag */
	public final static String ELT_STRONG = "strong";
	/** Html document style tag */
	public final static String ELT_STYLE = "style";
	/** Html subscript tag */
	public final static String ELT_SUB = "sub";
	/** Html details summary tag */
	public final static String ELT_SUMMARY = "summary";
	/** Html superscript tag */
	public final static String ELT_SUP = "sup";
	/** Html table tag */
	public final static String ELT_TABLE = "table";
	/** Html table body tag */
	public final static String ELT_TBODY = "tbody";
	/** Html table data column tag */
	public final static String ELT_TD = "td";
	/** Html template tag */
	public final static String ELT_TEMPLATE = "template";
	/** Html input text area tag */
	public final static String ELT_TEXTAREA = "textarea";
	/** Html tfoot tag */
	public final static String ELT_TFOOT = "tfoot";
	/** Html table header column tag */
	public final static String ELT_TH = "th";
	/** Html table head tag */
	public final static String ELT_THEAD = "thead";
	/** Html time tag */
	public final static String ELT_TIME = "time";
	/** Html document title tag */
	public final static String ELT_TITLE = "title";
	/** Html table row tag */
	public final static String ELT_TR = "tr";
	/** Html media track tag */
	public final static String ELT_TRACK = "track";
	/** Html teletype text tag (deprecated) */
	public final static String ELT_TT = "tt";
	/** Html special formatting tag */
	public final static String ELT_U = "u";
	/** Html unordered list tag */
	public final static String ELT_UL = "ul";
	/** Html variable definition tag */
	public final static String ELT_VAR = "var";
	/** Html video tag */
	public final static String ELT_VIDEO = "video";
	/** Html word-break opportunities tag */
	public final static String ELT_WBR = "wbt";
	/** Html xmp tag */
	public final static String ELT_XMP = "xmp";
}
