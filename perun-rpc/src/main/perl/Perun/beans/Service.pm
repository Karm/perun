package Perun::beans::Service;

use strict;
use warnings;

use Perun::Common;

use overload
    '""' => \&toString;

sub toString {
    my $self = shift;
    
    my $id = $self->{_id};
    my $name = $self->{_name};
    
    my $str = 'Service (';
    $str .= "id: $id, " if ($id);
    $str .= "name: $name, " if ($name);
    $str .= ')';
    
    return $str;
}

sub new
{
    bless({});
}

sub fromHash
{
    return Perun::Common::fromHash(@_);
}

sub TO_JSON
{
	my $self = shift;
	
	my $id;
	if (defined($self->{_id})) {
		$id = $self->{_id}*1;
	} else {
		$id = 0;
	}
	
	my $name;
	if (defined($self->{_name})) {
		$name = "$self->{_name}";
	} else {
		$name = undef;
	}
	
	return {id => $id, name => $name};
}

sub getId
{
    my $self = shift;
    
    return $self->{_id};
}

sub setId
{
    my $self = shift;
    $self->{_id} = shift;
    
    return;
}

sub getName
{
    my $self = shift;
    
    return $self->{_name};
}

sub setName
{
    my $self = shift;
    $self->{_name} = shift;
    
    return;
}


1;
