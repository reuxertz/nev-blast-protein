#!C:/Perl/bin/perl.exe 
##############################
#########General Info#########
##############################
# UW-Parkside, Dr. Robert Barber's Lab
# Shay Downey-Forsythe, 2009/10
# sdforsythe@gmail.com

# The purpose of this program is to aid in the identification of natural enzyme variants.

# This program takes a blast parse file and compares active/binding site signatures from a query signature against the resulting sequences. 
# It scores the results based on signature similarity by using a BLOSOM50 matrix. 
# Results are returned in a tab delimited file.

# The program also return scores for PAM250 and GONNET matrices, return results in XML, and score two or more signatures. (Other version)

##############################
#######Import  Packages#######
##############################

##############################
##########Utilities###########
##############################
#use Data::Dumper;
  use Win32::Console::ANSI;
  use Term::ANSIColor;
  use Bio::Tools::Run::RemoteBlast;
  use Bio::SearchIO; 
  use Getopt::Std;

##############################
#######Global Variables#######
##############################
#my $fastaFile = "NP_579448.1.fasta";
#my $fastaFile = "YP_002525963.1.fasta";
my $fastaFile = "sample.fasta";
my $session = "sample";
my $blastFile = $session."_final_blast_parse.txt";
#my $signature1 = 'E 12 D 13 V 14 I 17 Y 18 H 21 R 22 K 24 E 25 E 26 H 44 G 45 Y 46 P 151 A 152 D 153 L 154 Y 155 R 159 V 162';
my $sigTextAreaData = "K 43 R 71 E 74 R 75 R 77 L 78 H 101 Q 104 I 107 S 108 Y 120 P 121 G 122 I 123 K 124 D 125 D 126 N 129 A 130\nE 12 D 13 V 14 I 17 Y 18 H 21 R 22 K 24 E 25 E 26 H 44 G 45 Y 46 P 151 A 152 D 153 L 154 Y 157 R 159 V 162"; #total score = 111
$sigTextAreaData =~ s/,//gi;
my @userSigs;
@userSigs = split("\n",$sigTextAreaData);
$numSigs = 25;
#my $signature2 = 'E 12 D 13 V 14 I 17 Y 18 H 21 R 22 K 24 E 25 E 26 H 44 G 45 Y 46 P 151 A 152 D 153 L 154 Y 155 R 159 V 162'; #total score = 120
my $outputfile = "test.tab";

##############################
########## Run me. ###########
##############################

my $totalSigs = $#userSigs+1;
my $c = 0;
if ($totalSigs < $numSigs)
{
	my @newSigs = getSugSigs($fastaFile);
	#foreach my $thing(@newSigs){print $thing."\n";}
	while ($totalSigs < $numSigs)
	{
		#this will return duplicate sigs if not compared to the current @signatures array
		$ns = $newSigs[$c];
		if ($ns !~ m/NONE FOUND/){push (@userSigs, $ns);}
		$c++;
		$totalSigs++;
	}
	
}
my @signatures;
foreach my $newSig(@userSigs)
{
	my $nsId = "";
	my @nsSplit = "";
	my $nsSig = "";
	if ($newSig =~ m/\|/)
	{	
		@nsSplit = split(/\|/,$newSig);
		$nsSig = $nsSplit[0];
		$nsId = $nsSplit[1];
	} else {	
		$nsSig = $newSig;
		$nsId = "";
	}
	print "Signature: ".$nsId."\t".$nsSig."\n";
	push (@signatures, $nsSig);
}
	

exit;

my @hashArr;
foreach my $item(@signatures)
{													print "Arr Item: ".$item."\n";
my %sigMainHash = "";
%sigMainHash = signatureHash($item);
foreach $key (sort (keys(%sigMainHash))) {}#print "$key \t$sigMainHash{$key}\n";}
$sigMainHash{'sigMax'} = maxScore(1,\%sigMainHash);
#%sigHash1 = signatureHash($signature1); 								# create a hash of positions and residues for each signature										
#foreach $key (sort (keys(%sigHash1))) {print "$key \t$sigHash1{$key}\n";} 				# sort and print each position and residue in the signature to the screen
#my $sigMax1 = maxScore(1,\%sigHash1); 									# score the max score for the signature
push(@hashArr, \%sigMainHash);										print  "sig score: ".$sigMainHash{'sigMax'}."\n";
}													


#%sigHash2 = signatureHash($signature2); 								# create a hash of positions and residues for each signature										
#foreach $key (sort (keys(%sigHash2))) {print "$key \t$sigHash2{$key}\n";} 				# sort and print each position and residue in the signature to the screen
#my $sigMax2 = maxScore(1,\%sigHash2); 									# score the max score for the signature
													#print  "sig score: ".$sigMax2."\n";<stdin>; 
													
my @hashRefArr = prepareResults($blastFile); 								# open the blast parse file and puts each element name and value into a hash (one per accession), then puts the hash refrences into an array

my $runtimes=0;

open(OUTFILE,">>$outputfile")||die "Cannot open $outputfile ";						# open the output file

print OUTFILE "Accession\t"; 
print OUTFILE "Name\t"; 
print OUTFILE "EValue\t";
print OUTFILE "Len\t";
print OUTFILE "Query\t"; 
print OUTFILE "Subject\t"; 
my $sigCount = 0;
while ($sigCount<=$#signatures)
{
	print OUTFILE "Sig\t"; 
	print OUTFILE "Score\t"; 
	$sigCount++;
}
print OUTFILE "\n"; 

					
	for (my $i = 0; $i < @hashRefArr; $i++) 
	{
		my $temp1 = $hashRefArr[$i];								# get the name of the hash from the array
		my %temp2 = %$temp1;									# reference the hash name so we can use the hash
		my %scoreMe = ();									# create a hash we will use to hold query signature residues and the corresponding alignment residue				
		my $subsig = "";									# create a string to hold the aligned sequence's corresponding signature residues
#		my $subsig2 = "";
		my $newSubsig = "";									# create a string to hold the aligned sequence's corresponding signature residues
#		my $newSubsig2 = "";
		my $queryStart = $temp2{'queryStart'};
		my $subjectStart = $temp2{'subjectStart'};
		my $name = $temp2{'name'};
		my $ac = $temp2{'accession'}; print $ac."\n";
		my $eval = $temp2{'eVal'};								
		$eval =~ s/,//gi;
		if ($eval==0)										# This statement corrects a problem where evalues that are 0 are not
		{											#	formatted as #E#
			$eval = '0E-0';
		}											#print "EV: ".$eval."\t";
		my $len = $temp2{'len'};
		my $q = $temp2{'query'}; #print $q."\n";
		my @ArrQ = split(//,$q);
		my $s = $temp2{'subject'}; #print $s."\n\n";
		my @ArrS = split(//,$s);
		
		my @sitesArr;
		my @sitesArrQ;
		my @sitesArrS; 
		
		my $realResCount = $queryStart;
		
		print OUTFILE $ac."\t"; 
		print OUTFILE $name."\t"; 
		print OUTFILE $eval."\t";
		print OUTFILE $len."\t";
		print OUTFILE $q."\t"; 
		print OUTFILE $s."\t"; 
		
		foreach my $item(@hashArr)
		{		
			$realResCount = $queryStart;
			my %sigHash = %$item;						
			foreach my $key (sort (keys(%sigHash))) 							
			{	
				#print $key." ".$sigHash{$key}.", ";
				my $newPosition = $key-$queryStart;
				if ($newPosition >=0)
				{
					push(@sitesArr,$newPosition);
				}
			}
			print "\n";
			my $site = shift @sitesArr;
			my $hyphens = 0;
			my $thingyCounter=0;	
			#my $realResCount = $queryStart;
			
			print $queryStart." ";
			print $#ArrQ." ";
			print $site;
			print "\n\t";
			while ($thingyCounter <= $#ArrQ)
			{
				#print "'";
				#print $ArrQ[$thingyCounter];
				#print $thingyCounter;
				#print $realResCount;
				#print "'";
				
				my $bool=0;
				if ($ArrQ[$thingyCounter] eq "-"){$hyphens++;$bool=1;print "*";}else{$realResCount++;}
				if ($bool==0)
				{
					my $mod = $realResCount % 10;
					if ($thingyCounter-$hyphens ==  $site)
					{
						#print $site;
						#print "'";
						my $oldValue = $site+$queryStart;
						while (length $oldValue < 6){$oldValue="0".$oldValue;}
						#print $oldValue;
						print $sigHash{$oldValue};
						push(@sitesArrQ,$site);
						$site = shift @sitesArr;
					}
					elsif ($mod == 1){print "|";} 
					else {print " ";}
				}
				$thingyCounter++;
			}
			print "\n\t";
			my $siteQ = shift @sitesArrQ;
			$thingyCounter=0;
			$hyphens = 0;
			while ($thingyCounter <= $#ArrQ)
			{
				if ($ArrQ[$thingyCounter] eq "-"){$hyphens++;}
				if ($thingyCounter-$hyphens ==  $siteQ)
				{
					print color 'bold green'; print $ArrQ[$thingyCounter]; print color 'reset';
					push(@sitesArrS,$siteQ);
					$siteQ = shift @sitesArrQ;
				}
				else
				{
					print $ArrQ[$thingyCounter];
				}
				$thingyCounter++;
			}
			print "\n\t";
			my $siteS = shift @sitesArrS;
			$thingyCounter=0;
			$hyphens = 0;
			my $subsig = "";
			while ($thingyCounter <= $#ArrS)
			{
				if ($ArrQ[$thingyCounter] eq "-"){$hyphens++;}
				if ($thingyCounter-$hyphens ==  $siteS)
				{
					print color 'bold green'; print $ArrS[$thingyCounter]; print color 'reset';
					$subsig .= $ArrS[$thingyCounter];
					my $helper = $siteS+$queryStart;
					while (length $helper < 6){$helper="0".$helper;}
					$scoreMe{$helper} = $ArrS[$thingyCounter];
					$siteS = shift @sitesArrS;
				}
				else
				{
					print $ArrS[$thingyCounter];
				}
				$thingyCounter++;
			}
			print "\n\n";
		my $keyCount = keys %scoreMe;								#print "\n Total Keys: ".$keyCount."\n"; 	
		my $subjectMax = maxScore(2,\%scoreMe,\%sigHash);					# send the result signature to be scored and the sequence to score it against													#print $subjectMax."\t"; 
		my $finalScore = $subjectMax/$sigHash{'sigMax'}; 						# calculate the ratio of the query and result scores		
		print "SUBSIG: ".$subsig."\n";
		print $sigHash{'sigMax'}." ".$subjectMax." ".$finalScore."\n\n";
		print OUTFILE $subsig."\t"; 
		print OUTFILE $finalScore."\t"; 
		}
		print OUTFILE "\n"; 									# LOOP!
#		my $keyCount = keys %scoreMe;								#print "\n Total Keys: ".$keyCount."\n"; 	
#		my $subjectMax = maxScore(2,\%scoreMe,\%sigHash);					# send the result signature to be scored and the sequence to score it against													#print $subjectMax."\t"; 
#		my $finalScore = $subjectMax/$sigHash{'sigMax'}; 						# calculate the ratio of the query and result scores		
#		print "FINAL SCORE: ".$finalScore."\n\n";

		$runtimes++;
		#if ($runtimes>1){exit;}
	}											

print "Done: ".$runtimes;
exit;
##############################
#########SUBROUTINES##########
##############################

sub getSugSigs()
{
	my $query_file = $_[0];
	my $session_num = $session;
	my $prog = 'blastp';
	my $db   = 'pdb';
	my $number_of_alignments = '100';
	
	my @params = ( '-prog' => $prog,
	       '-data' => $db,
	       '-readmethod' => 'SearchIO');
	
	my $factory = Bio::Tools::Run::RemoteBlast->new(@params);
	
	#change a retrieval parameter
	$Bio::Tools::Run::RemoteBlast::RETRIEVALHEADER{'DESCRIPTIONS'} = 50;
	
	my $v = 1;
	#$v is just to turn on and off the messages
	
	#change a retrieval parameter
	$Bio::Tools::Run::RemoteBlast::HEADER{'DESCRIPTIONS'} = $number_of_alignments;
	#$Bio::Tools::Run::RemoteBlast::HEADER{'ALIGNMENTS'} = $number_of_alignments;
	$Bio::Tools::Run::RemoteBlast::RETRIEVALHEADER{'DESCRIPTIONS'} = $number_of_alignments;
	#$Bio::Tools::Run::RemoteBlast::RETRIEVALHEADER{'ALIGNMENTS'} =$number_of_alignments;
	
	# USERINPUT OF AMINO ACID SEQUENCE          
	my $r = $factory->submit_blast($query_file);           
	
	
	while ( my @rids = $factory->each_rid )
	{
		foreach my $rid ( @rids )
		{
			my $rc = $factory->retrieve_blast($rid);
			if( !ref($rc) )
			{
				if( $rc < 0 )
				{
					$factory->remove_rid($rid);
				}
	
			}
			else
			 {
			$factory->save_output($session_num . "temp.txt");
	 
				my $checkinput = $factory->file;
				open(my $fh,"<$checkinput") or die $!;
				close $fh;
	 
	 
				$factory->remove_rid($rid);
			 }
		 }
	}
	
	
	my $inputfile = $session_num . "temp.txt";
	open(FILE,"<$inputfile") || die "Could not open file: $inputfile";
	my @pdbs = <FILE>;
	close FILE;
	my $outputfile = $session_num . "pdb_parse";
	
	my @ids;
	my $line = shift(@pdbs);
	foreach my $line(@pdbs)
	{
		if ($line =~ m/^pdb/)
		{;
			my @arr = split(/\|/,$line);
			my $pdbID = $arr[1]."|".substr($arr[2],0,1);
			push  (@ids, $pdbID);
			print "arr: ".$pdbID."\n";
		}
	}
	
	my $csa = "CSA_2_2_12.csv";
	open(FILE,"<$csa") || die "Could not open file: $csa";
	my @csaEntries = <FILE>;
	close FILE;
	
	# Amino acid Table
	#	http://www.ncbi.nlm.nih.gov/Class/MLACourse/Modules/MolBioReview/iupac_aa_abbreviations.html
	my %AminoAcids =(
		Ala => "A",     #Alanine
		Arg => "R",     #Arginine
		Asn => "N",     #Asparagine
		Asp => "D",     #Aspartic acid (Aspartate)
		Cys => "C",     #Cysteine
		Gln => "Q",     #Glutamine
		Glu => "E",     #Glutamic acid (Glutamate)
		Gly => "G",     #Glycine
		His => "H",     #Histidine
		Ile => "I",     #Isoleucine
		Leu => "L",     #Leucine
		Lys => "K",     #Lysine
		Met => "M",     #Methionine
		Phe => "F",     #Phenylalanine
		Pro => "P",     #Proline
		Ser => "S",     #Serine
		Thr => "T",     #Threonine
		Trp => "W",     #Tryptophan
		Tyr => "Y",     # Tyrosine
		Val => "V",     #Valine
		Asx => "B",     #Aspartic acid or Asparagine
		Glx => "Z",     #Glutamine or Glutamic acid.
		Xaa => "X"     #Any amino acid.
		#TERM            termination codon
		);
	
	my @suggestedSigs;
	
	foreach my $idPDB(@ids)
	{
		print "pdb: ".$idPDB."\n";
		my @idArr = split(/\|/,$idPDB);
		my $id = $idArr[0];
		print "id: ".$id."\t";
		my $chain = $idArr[1];
		print "chain: ".$chain."\n";
		#my $x = <STDIN>; chomp $x; if ($x eq "q"){exit;}
		print @csaEntries."\n";
		my @grepCSAEntries = grep(/^($id),[0-9]+,[A-Z]{3},($chain)/i, @csaEntries);
		#my @grepCSAEntries = grep(/($id)/i, @csaEntries);
		print "entries: ".@grepCSAEntries."\n\n";
		my $newSig = "";
		foreach my $grep(@grepCSAEntries)
		{
			
			my @grepArr = split(',', $grep);
			my $aaIn = $grepArr[2];
			$aaIn = lc($aaIn);
			$aaIn = ucfirst($aaIn);
			my $aaPos = $grepArr[4];
			my $aa = $AminoAcids{$aaIn};
			print "grep: ".$grep;
			print "aaIn: ".$aaIn."\n";
			$newSig .= $aa." ".$aaPos." ";
		}
		if ($newSig ne "")
		{
			print $newSig."\n";
			push (@suggestedSigs, $newSig."|".$id.$chain);
		}	
	}
	my @returnSigs;
	my $c = 0;
	while ($c <= $#suggestedSigs)
	{
		my $isUnique = 1;
		my $testUnique = shift @suggestedSigs;
		my @splitUnique = split(/\|/,$testUnique);
		my $unique = $splitUnique[0];
		print $unique."\n";
		foreach my $ssig(@returnSigs)
		{
			$unique =~ s/\s$//;
			$ssig =~ s/\s$//;
			print "u: ".$unique."\n";
			print "s: ".$ssig."\n";
			print $unique." : ".$ssig."\n";
			if ($unique eq $ssig)
			{
				print $unique." = ".$ssig."\n";
				print $#suggestedSigs."\n";
				$isUnique = 0;
			} 
		}
		if ($isUnique == 1)
		{
			print $testUnique."\n";
			push (@returnSigs, $testUnique);
		}
		if ($#returnSigs == -1)
		{
			print "NONE FOUND";
			$testUnique = "NONE FOUND";
			push (@returnSigs, $testUnique);
		}
	}
	return @returnSigs;
}

###################################################
# builds an array of hash references for each blast result
# since the format of the result file is known, we can loop at every ninth line (the last two lines are blank)
sub prepareResults()
{
	my $filename = $_[0];
	open(FILE,"<$filename") || die "Could not open file: $filename";				# open the blast parse file
	my @blast = <FILE>;										# read each line of the file into an array
	close FILE;											# close the blast parse file
	
	my $arrSize = @blast;
	my @hashRefArr = ();										# the array to hold the hashmaps
	my $line;
	for (my $blockCounter=0; $blockCounter<($arrSize / 9); $blockCounter++) {			# divide the array into blocks of 9 lines and loop through each block
		my %blockHash = ();									# create a new hash to hold information from each block
		for (my $lineCounter=0; $lineCounter<9; $lineCounter++) {				# loop through to grab information from each of nine lines
			my $count = ($blockCounter * 9) + $lineCounter;					# morbid curiousity, has no real function
			$line = $blast[$count];								# get the current line
			chomp($line);									# get rid of the \n at the end of the line
			if ($lineCounter == 0) {							
				my @words = split('= ', $line);
				$blockHash{'name'} = $words[1];						# add the name of the result to the hash
			} elsif ($lineCounter == 1) {
				my @words = split(' = ', $line);
				$blockHash{'accession'} = $words[1];					# add the accession number of the result to the hash
			} elsif ($lineCounter == 2) {
				my @words = split(' = ', $line);
				$blockHash{'len'} = $words[1];						# add the length of the result to the hash
			} elsif ($lineCounter == 3) {
				my @words = split(' = ', $line);
				$blockHash{'eVal'} = $words[1];						# add the E-value of the result to the hash
			} elsif ($lineCounter == 4) {							# This line has 3 different bits of info we need, so we split it.
				my @words = split(' = ', $line);
				my @subWords = split('	', $words[1]);
				$blockHash{'queryStart'} = $subWords[0];				# add the start position of the query alignment to the hash
				$blockHash{'query'} = $subWords[1];					# add the sequence of the query alignment to the hash
				$blockHash{'queryStop'} = $subWords[2];					# add the stop position of the query alignment to the hash
			} elsif ($lineCounter == 5) {
				my @words = split(' = 	', $line);
				$blockHash{'homol'} = $words[1];					# unused
			} elsif ($lineCounter == 6) {							# This line has 3 different bits of info we need, so we split it.
				my @words = split(' = ', $line);
				my @subWords = split('	', $words[1]);
				$blockHash{'subjectStart'} = $subWords[0];				# add the start position of the result alignment to the hash
				$blockHash{'subject'} = $subWords[1];					# add the sequence of the result alignment to the hash
				$blockHash{'subjectStop'} = $subWords[2];				# add the stop position of the result alignment to the hash
				push (@hashRefArr, \%blockHash);					# push the completed hash into the hash array
			}										
		}											# LOOP!
	}												# LOOP!

	###Checks the HASHes to make sure we have what we want###
	#for (my $i = 0; $i < @hashRefArr; $i++) {
	#	my $temp1 = $hashRefArr[$i];
	#	my %temp2 = %$temp1;
	#	foreach my $key (keys %temp2) 
	#	{
	#		print $key.": ".$temp2{$key}. "\n"; 
	#	}
	#}
	
	return @hashRefArr;										# return the array of hashes
}

###################################################
# Parse signatures into arrays.
# Takes an alpha-numeric string.
# Returns arrays of the position and amino acid ID 
# for each amino acid in the string in the correct formaat.

sub signatureHash()											# currently assumes either a letter followed by any # of digits or vice-versa
{
	my ($strIn) = $_[0];
	my $strTemp;											# create a temporary string of digits until we have all of them between letters
	my @d; 												# digit array
	my @l; 												# letter array
	my %temp;											#print "strIn: ".$strIn."\n";
	$strIn =~ s/\s//gi;										# get rid of spaces if the user entered them
	my @chars = split(//, $strIn);									# split the string into an array at each character
	my $arrLen = $#chars;										# get the length of the array
	for (my $i=0; $i<=$arrLen; $i++)								# loop through the array to find digits (positions) and letters (residues)
	{
		my $char = @chars[$i];									# get the current character
		if ($char =~ m/[0-9]/)									# if it's a digit
		{
			$strTemp = '';									# empty the string
			while ($char =~ m/[0-9]/)							# as long as we're getting digits
			{
				$strTemp .= $char;							# concatonate the digit to the string
				$i++;									# go to the next index position	
				$char = @chars[$i];							# get the character from the array
			}										# LOOP!	
			my $strLen = length $strTemp;							# This my/if that follows corrects a problem where PERL cannot sort by
			if ($strLen < 6 && $strLen > -1)								#	integer value when sorting hashes. PERL sorts by ASCII values.
			{
				$numZeros = 6 - $strLen;
				for (my $x=0; $x<$numZeros; $x++)
				{
					$strTemp = '0'.$strTemp;
				}
			}
			push(@d, $strTemp);								# push the string into the digit array once we have a complete position
		}		
		if ($char =~  m/[A-Za-z]/)								# if it's a letter, it can only ahve a length of 1 anyway... so...
		{				
			push(@l, $char);								# push the letter into the letter array										#print $char."\t";	
		}
	}
	if ($#d != $#l){										# if each letter doesn't have a position (and vice-versa) we can't continue
		die "The number of positions does not match the number of letters.";
	}
	else												# otherwise
	{
		@temp{@d} = @l;										# add the position and residue to the %temp hash
	}	
	return %temp;											# return the hash
}

###########################################################
#DETERMINE MAX SCORE FOR USER INPUT RESIDUES

sub maxScore()					
{
	my $action = $_[0];
	my %h = %{$_[1]};
	my %sigHash;	
	%sigHash = %{$_[2]};
	
	my(%BLOSUM) = (											# A BLOSOM50 matrix in bloom...
	'C' => {'-'=> '-20', 'C|c' => '9', 'S|s' => '-2', 'T|t' => '-1','P|p' => '-4','A|a' => '-1', 'G|g' => '-4','N|n' => '-3','D|d' => '-4', 'E|e' => '-5', 'Q|q' => '-4','H|h' => '-4','R|r' => '-4', 'K|k' => '-4','M|m' => '-2','I|i' => '-2', 'L|l' => '-2','V|v' => '-1','F|f' => '-3', 'W|w' => '-3','Y|y' => '-3'},
	'S' => {'-'=> '-20', 'C|c' => '-2', 'S|s' => '5', 'T|t' => '1','P|p' => '-1','A|a' => '1', 'G|g' => '-1','N|n' => '0','D|d' => '-1', 'E|e' => '0', 'Q|q' => '0','H|h' => '-1','R|r' => '-1', 'K|k' => '-1','M|m' => '-2','I|i' => '-3', 'L|l' => '-3','V|v' => '-2','F|f' => '-3', 'W|w' => '-4','Y|y' => '-2'},
	'T' => {'-'=> '-20', 'C|c' => '-1', 'S|s' => '1', 'T|t' => '5','P|p' => '-2','A|a' => '0', 'G|g' => '-2','N|n' => '0','D|d' => '-1', 'E|e' => '-1', 'Q|q' => '-1','H|h' => '-2','R|r' => '-1', 'K|k' => '-1','M|m' => '-1','I|i' => '-1', 'L|l' => '-2','V|v' => '0','F|f' => '-2', 'W|w' => '-4','Y|y' => '-2'},
	'P' => {'-'=> '-20', 'C|c' => '-4', 'S|s' => '-1', 'T|t' => '-2','P|p' => '8','A|a' => '-1', 'G|g' => '-3','N|n' => '-3','D|d' => '-2', 'E|e' => '-2', 'Q|q' => '-2','H|h' => '-3','R|r' => '-2', 'K|k' => '-1','M|m' => '-3','I|i' => '-4', 'L|l' => '-3','V|v' => '-3','F|f' => '-4', 'W|w' => '-5','Y|y' => '-4'},
	'A' => {'-'=> '-20', 'C|c' => '-1', 'S|s' => '1', 'T|t' => '0','P|p' => '-1','A|a' => '5', 'G|g' => '0','N|n' => '-2','D|d' => '-2', 'E|e' => '-1', 'Q|q' => '-1','H|h' => '-2','R|r' => '-2', 'K|k' => '-1','M|m' => '-1','I|i' => '-2', 'L|l' => '-2','V|v' => '0','F|f' => '-3', 'W|w' => '-3','Y|y' => '-2'},
	'G' => {'-'=> '-20', 'C|c' => '-4', 'S|s' => '-1', 'T|t' => '-2','P|p' => '-3','A|a' => '0', 'G|g' => '6','N|n' => '-1','D|d' => '-2', 'E|e' => '-3', 'Q|q' => '-2','H|h' => '-3','R|r' => '-3', 'K|k' => '-2','M|m' => '-4','I|i' => '-5', 'L|l' => '-4','V|v' => '-4','F|f' => '-4', 'W|w' => '-4','Y|y' => '-4'},
	'N' => {'-'=> '-20', 'C|c' => '-3', 'S|s' => '0', 'T|t' => '0','P|p' => '-3','A|a' => '-2', 'G|g' => '-1','N|n' => '6','D|d' => '1', 'E|e' => '-1', 'Q|q' => '0','H|h' => '0','R|r' => '-1', 'K|k' => '0','M|m' => '-3','I|i' => '-4', 'L|l' => '-4','V|v' => '-4','F|f' => '-4', 'W|w' => '-4','Y|y' => '-3'},
	'D' => {'-'=> '-20', 'C|c' => '-4', 'S|s' => '-1', 'T|t' => '-1','P|p' => '-2','A|a' => '-2', 'G|g' => '-2','N|n' => '1','D|d' => '6', 'E|e' => '1', 'Q|q' => '-1','H|h' => '-2','R|r' => '-2', 'K|k' => '-1','M|m' => '-4','I|i' => '-4', 'L|l' => '-5','V|v' => '-4','F|f' => '-4', 'W|w' => '-6','Y|y' => '-4'},
	'E' => {'-'=> '-20', 'C|c' => '-5', 'S|s' => '0', 'T|t' => '-1','P|p' => '-2','A|a' => '-1', 'G|g' => '-3','N|n' => '-1','D|d' => '1', 'E|e' => '6', 'Q|q' => '2','H|h' => '0','R|r' => '-1', 'K|k' => '1','M|m' => '-2','I|i' => '-4', 'L|l' => '-4','V|v' => '-3','F|f' => '-4', 'W|w' => '-4','Y|y' => '-3'},
	'Q' => {'-'=> '-20', 'C|c' => '-4', 'S|s' => '0', 'T|t' => '-1','P|p' => '-2','A|a' => '-1', 'G|g' => '-2','N|n' => '0','D|d' => '-1', 'E|e' => '2', 'Q|q' => '6','H|h' => '1','R|r' => '1', 'K|k' => '1','M|m' => '0','I|i' => '-3', 'L|l' => '-3','V|v' => '-3','F|f' => '-4', 'W|w' => '-3','Y|y' => '-2'},
	'H' => {'-'=> '-20', 'C|c' => '-4', 'S|s' => '-1', 'T|t' => '-2','P|p' => '-3','A|a' => '-2', 'G|g' => '-3','N|n' => '0','D|d' => '-2', 'E|e' => '0', 'Q|q' => '1','H|h' => '8','R|r' => '0', 'K|k' => '-1','M|m' => '-2','I|i' => '-4', 'L|l' => '-3','V|v' => '-4','F|f' => '-2', 'W|w' => '-3','Y|y' => '2'},
	'R' => {'-'=> '-20', 'C|c' => '-4', 'S|s' => '-1', 'T|t' => '-1','P|p' => '-2','A|a' => '-2', 'G|g' => '-3','N|n' => '-1','D|d' => '-2', 'E|e' => '-1', 'Q|q' => '1','H|h' => '0','R|r' => '6', 'K|k' => '2','M|m' => '-2','I|i' => '-3', 'L|l' => '-3','V|v' => '-3','F|f' => '-4', 'W|w' => '-4','Y|y' => '-3'},
	'K' => {'-'=> '-20', 'C|c' => '-4', 'S|s' => '-1', 'T|t' => '-1','P|p' => '-1','A|a' => '-1', 'G|g' => '-2','N|n' => '0','D|d' => '-1', 'E|e' => '1', 'Q|q' => '1','H|h' => '-1','R|r' => '2', 'K|k' => '5','M|m' => '-2','I|i' => '-3', 'L|l' => '-3','V|v' => '-3','F|f' => '-4', 'W|w' => '-4','Y|y' => '-3'},
	'M' => {'-'=> '-20', 'C|c' => '-2', 'S|s' => '-2', 'T|t' => '-1','P|p' => '-3','A|a' => '-1', 'G|g' => '-4','N|n' => '-3','D|d' => '-4', 'E|e' => '-2', 'Q|q' => '0','H|h' => '-2','R|r' => '-2', 'K|k' => '-2','M|m' => '6','I|i' => '1', 'L|l' => '2','V|v' => '1','F|f' => '0', 'W|w' => '-2','Y|y' => '-2'},
	'I' => {'-'=> '-20', 'C|c' => '-2', 'S|s' => '-3', 'T|t' => '-1','P|p' => '-4','A|a' => '-2', 'G|g' => '-5','N|n' => '-4','D|d' => '-4', 'E|e' => '-4', 'Q|q' => '-3','H|h' => '-4','R|r' => '-3', 'K|k' => '-3','M|m' => '1','I|i' => '5', 'L|l' => '1','V|v' => '3','F|f' => '-1', 'W|w' => '-3','Y|y' => '-2'},
	'L' => {'-'=> '-20', 'C|c' => '-2', 'S|s' => '-3', 'T|t' => '-2','P|p' => '-3','A|a' => '-2', 'G|g' => '-4','N|n' => '-4','D|d' => '-5', 'E|e' => '-4', 'Q|q' => '-3','H|h' => '-3','R|r' => '-3', 'K|k' => '-3','M|m' => '2','I|i' => '1', 'L|l' => '4','V|v' => '1','F|f' => '0', 'W|w' => '-2','Y|y' => '-2'},
	'V' => {'-'=> '-20', 'C|c' => '-1', 'S|s' => '-2', 'T|t' => '0','P|p' => '-3','A|a' => '0', 'G|g' => '-4','N|n' => '-4','D|d' => '-4', 'E|e' => '-3', 'Q|q' => '-3','H|h' => '-4','R|r' => '-3', 'K|k' => '-3','M|m' => '1','I|i' => '3', 'L|l' => '1','V|v' => '4','F|f' => '-1', 'W|w' => '-3','Y|y' => '-2'},
	'F' => {'-'=> '-20', 'C|c' => '-3', 'S|s' => '-3', 'T|t' => '-2','P|p' => '-4','A|a' => '-3', 'G|g' => '-4','N|n' => '-4','D|d' => '-4', 'E|e' => '-4', 'Q|q' => '-4','H|h' => '-2','R|r' => '-4', 'K|k' => '-4','M|m' => '0','I|i' => '-1', 'L|l' => '0','V|v' => '-1','F|f' => '6', 'W|w' => '0','Y|y' => '3'},
	'W' => {'-'=> '-20', 'C|c' => '-3', 'S|s' => '-4', 'T|t' => '-4','P|p' => '-5','A|a' => '-3', 'G|g' => '-4','N|n' => '-4','D|d' => '-6', 'E|e' => '-4', 'Q|q' => '3','H|h' => '3','R|r' => '-4', 'K|k' => '-4','M|m' => '-2','I|i' => '-3', 'L|l' => '-2','V|v' => '-3','F|f' => '0', 'W|w' => '11','Y|y' => '2'},
	'Y' => {'-'=> '-20', 'C|c' => '-3', 'S|s' => '-2', 'T|t' => '-2','P|p' => '-4','A|a' => '-2', 'G|g' => '-4','N|n' => '-3','D|d' => '-4', 'E|e' => '-3', 'Q|q' => '-2','H|h' => '2','R|r' => '-3', 'K|k' => '-3','M|m' => '-2','I|i' => '-2', 'L|l' => '-2','V|v' => '-2','F|f' => '3', 'W|w' => '2','Y|y' => '7'}
	);
	
	my $max_score = 0;										# set the maxScore to 0 every time we run the scoring function
	
	if ($action==1)											# score the query signatures
	{
		for (keys %h){
			my $character = $h{$_};
			foreach my $master (keys %BLOSUM) {						# loop through each matrix row in column 0
				if ($master =~ /$character/)  {						# if the query residue matches the residue in the row/column
					foreach my $key_one (keys %{$BLOSUM{$master}})  {		# loop through each column of that row	
						if ($key_one =~ /$character/)  {			# if the key matches the query residue
							$total = $BLOSUM{$master}{$key_one};		# get the value for that residue
							$max_score += $total;				# add the value for teh residue to the total score for the result signature
						}
					}								# LOOP!	
				}
			}										# LOOP!	
		}											# LOOP!	
	}
	elsif ($action==2)										# score the result signatures
	{
		foreach $h(sort (keys %h)){
			my $oldCharacter = $sigHash{$h};						
			my $character = $h{$h};								
			foreach my $master (keys %BLOSUM) {						# loop through each matrix row in column 0
				if ($master =~ /$oldCharacter/)  {					# if the query residue matches the residue in the row/column
					foreach my $key_one (keys %{$BLOSUM{$master}})  {		# loop through each column of that row
						if ($key_one =~ /$character/)  {			# if the key matches the result residue
							$total = $BLOSUM{$master}{$key_one};		# get the value for that residue
							print "OC: ".$oldCharacter."\t";
							print "NC: ".$character."\t";
							print "TV: ".$total."\n";
							$max_score += $total;				# add the value for teh residue to the total score for the result signature
						}
					}								# LOOP!	
				}
			}										# LOOP!	
		}											# LOOP!	
	}
	return $max_score;										# return the score			
}


print "DONE\n";
exit;

