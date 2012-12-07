#!/usr/bin/perl -w

# Front end for displaying ESA of a phrase/word,etc
## Adapted from CSP-demo.pl

# Use `standard' for backwards compatibility with procedural interface
# Use `escapeHTML' to parse user input that includes &, <, > and "
use CGI qw(:standard :escapeHTML);
use CGI::Carp 'fatalsToBrowser';  # redirect error messages to browser.
use IO::Socket;
use POSIX qw(tmpnam);

use FileHandle;
use IPC::Open2;

#$ProgramDir = "/home/roth/cogcomp/ratinov2/LbjNerTagger1.2";
#$Program = "$ProgramDir/nerDemo.pl";

$host = "hefty";
$port = "5177";

$logfile = ">>/home/roth/cogcomp/ratinov2/LbjNerTagger1.2/LbjNer.log";

$Demoname = "Named Entity Recognition with Lbj Taqgger";
$Header = "NER with Lbj Tagger";
$Url = "http://l2r.cs.uiuc.edu/~cogcomp/LbjNer.php";

select STDOUT;
$| = 1;

print_head();

$query = param('sentence');
$newlines = param('forceNewlines');
#$query =~ s/[\n\r]/ /g;


if ($query !~ /^\s*$/) {

    my $handle = IO::Socket::INET->new(Proto     => "tcp",
				       PeerAddr  => $host,
				       PeerPort  => $port)
	or die "can't connect to port $port on $host: $!";
    
    $handle->autoflush(1);		# so output gets there right away
    

# split the program into two processes, identical twins

    
    my $numConsecutiveNewLines = 0;

    my $numSent = 1;
    my $numReceived = 0;
    my $seenTextAfterSentUpdate = 0;

    print "<h3>Input Text:</h3>\n";
#    print pre($query), "\n";


    print "<textarea cols=80 rows = 10 readonly=\"readonly\" wrap=\"virtual\">";
    print  $query;
    print "</textarea>";
	

#     @sentences = split /\n/, $query;
#     $input = "";
#     foreach $line(@sentences) { 
# 	print $line;
# 	chomp $line; 
# 	$input .= $line;
#     }

     $query =~ s/\n/\*newline\*/g;
    $query =~ s/[\n\r]/ /g;    
    print $handle "*" . $newlines . "*\t" . $query." *end*\n";
    
#    print pre($input), "\n";


    

    print "<h1>Result:</h1>\n";

    print "<p>\n";

    while ((defined (my $line = <$handle>)) && ($numReceived < $numSent)) 
    {
	if ($line =~ /^\s+$/) {

	    $numConsecutiveNewLines++;
	    
	    #determine if this is the end of an example;
	    #update count of number of sentences received
	    
	    if(1 == $seenTextAfterSentUpdate && $numConsecutiveNewLines > 0) {
		
		$numReceived++;
		$seenTextAfterSentUpdate = 0;
		
	    }
	}
	else {
	    
	    $numConsecutiveNewLines = 0;
	    $seenTextAfterSentUpdate = 1;
	}

#  print STDERR "##numReceived is $numReceived...\n";
	print $line;
    }

    close $handle or die "Can't close socket handle: $!\n";

    print "\n";
    print "</p>\n";

}
print p,
    hr, 
    a({-href=>"$Url"}, "Back"), 
    end_html();

sub print_head() {

    my $head = << "end_of_head;";
    <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<title>$Header</title>
<link href="http://l2r.cs.uiuc.edu/~cogcomp/ccg.css" rel="stylesheet" type="text/css">
</head>

<body>
<h2>$DemoName Output</h2>

end_of_head;

print header(), $head;
}
