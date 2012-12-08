/* Generated By:JavaCC: Do not edit this line. HtmlLexerConstants.java */
package edu.stanford.nlp.annotation;

public interface HtmlLexerConstants {

  int EOF = 0;
  int COMMENT_START = 1;
  int COMMENT_END = 2;
  int COMMENT_WORD = 3;
  int ESCAPE_CHARACTER = 4;
  int CODE_TAG = 5;
  int CODE_END = 6;
  int CODE_WORD = 7;
  int HTML_TAG = 8;
  int RUNAWAY_TAG = 9;
  int HTML_INSIDE = 10;
  int HTML_QUOTED = 11;
  int WORD = 12;
  int APOST_WORD = 13;
  int EMAIL_WORD = 14;
  int DOT_WORD = 15;
  int DEC_COMMA_NUM = 16;
  int DEC_NUM = 17;
  int QUALIFIED_NUM = 18;
  int MONEY_NUM = 19;
  int NUM_LIST = 20;
  int TIME = 21;
  int ALPHANUM = 22;
  int ALPHA = 23;
  int DIGIT = 24;
  int NONZERO = 25;
  int HYPHEN = 26;
  int WHITESPACE = 27;
  int DOTPLUS = 28;
  int ANY = 29;

  int DEFAULT = 0;
  int COMMENT_BODY = 1;
  int CODE = 2;
  int TAG_NO_RETURN = 3;

  String[] tokenImage = {
    "<EOF>",
    "\"<!--\"",
    "\"-->\"",
    "<COMMENT_WORD>",
    "<ESCAPE_CHARACTER>",
    "<CODE_TAG>",
    "<CODE_END>",
    "<CODE_WORD>",
    "<HTML_TAG>",
    "<RUNAWAY_TAG>",
    "<HTML_INSIDE>",
    "<HTML_QUOTED>",
    "<WORD>",
    "<APOST_WORD>",
    "<EMAIL_WORD>",
    "<DOT_WORD>",
    "<DEC_COMMA_NUM>",
    "<DEC_NUM>",
    "<QUALIFIED_NUM>",
    "<MONEY_NUM>",
    "<NUM_LIST>",
    "<TIME>",
    "<ALPHANUM>",
    "<ALPHA>",
    "<DIGIT>",
    "<NONZERO>",
    "<HYPHEN>",
    "<WHITESPACE>",
    "<DOTPLUS>",
    "<ANY>",
  };

}