#!/usr/bin/perl

##################################################
# This code is almost identical to the code for the
# charniak server
##################################################

$MAXCHAR = 7999;
$MAXWORD = 4000;

$command = "./runDemo";
$endProtocol = "\n";

$TIMEOUT = 60;
$PORT = 5177;

$PORT = $ARGV[0] if (scalar(@ARGV) > 0);

use Expect;

#create main program that will be communicating throught pipe.
$main = NewExpect($command);

sub NewExpect {
    my $command = shift;
    my $main;

    print "[Initializing...]\n";

    $main = new Expect();
    $main->raw_pty(1);     # no local echo
    $main->log_stdout(0);  # no echo
    $main->spawn($command) or die "Cannot start: $command\n";

    $main->send("Test*end*\n");  #send input to main program
    @res = $main->expect(undef,$endProtocol);  # read output from main program

    $main->send("Test*end*\n");  #send input to main program
    @res = $main->expect(undef,$endProtocol);  # read output from main program

    print "[Done initializing.]\n";

    return $main;
}

#server initialization matter
use IO::Socket;
use Net::hostent;               # for OO version of gethostbyaddr

$server = IO::Socket::INET->new( Proto     => 'tcp',
                                 LocalPort => $PORT,
                                 Listen    => SOMAXCONN,
                                 Reuse     => 1);

die "Can't setup server\n" unless $server;
#end server initialization

#set autoflush
$old_handle = select(STDOUT);
$| = 1;
select($old_handle);
$old_handle = select(STDERR);
$| = 1;
select($old_handle);

print "[Server $0 accepting clients]\n";

%cache = ();

while ($client = $server->accept()) {
    $main->expect(0);  # flush old stuff if any
    $main->clear_accum();  # clear buffer

    $client->autoflush(1);
    $clientinfo = gethostbyaddr($client->peeraddr);
    if (defined($clientinfo)) {
	$clientname = ($clientinfo->name || $client->peerhost);
    } else {
	$clientname = $client->peerhost;
    }
    printf "[Connect from %s]\n", $clientname;

    &RunClient($client);

    shutdown($client,3);
    close($client);
    printf "[Connection closed from %s]\n", $clientname;
}

$main->hard_close();

sub RunClient {
    my $client = shift;
    my $msg;
    my $output;
    my @res;
    my $timeout;
    my $sent;

    $msg="";
    print "Reading from client\n";
    while ($sent = <$client>) {
	print "String read from the client: $sent\n";
	$msg = $msg . " $sent";
	if($sent =~ /\*end\*/){
	    last;
	}
    }

    $msg=substr($msg,0,length($msg)-6);
    print "Done reading from the client\n";
   
    print "start of input, this is just a test!!!\n";
    print "Input: $msg\n";
    print "end of input, this is just a test!!!\n";

    $main->send("$msg\n");  #send input to main program
    @res = $main->expect($TIMEOUT,$endProtocol);  # read output from main program

    $timeout = $res[1];
    $out = $res[3];
    if ($timeout) { # some problem, restart
	print "Time out!\n";
	$output = "\n\n";  # output blank
	print "Restart...\n";
	$main->hard_close();
	$main = NewExpect($command);
    }

    print "Output: $out";
    print $client $out;
    $main->clear_accum();  # clear buffer
}
