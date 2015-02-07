% parses the raw data obtained from calling getRawData


% returns one line of the file, for a given row
%@param row - a valid row within the cell array source
%@param source - the cell array to get the string from
%@return singleString - 
function [singleString] = getEntryAtLine(row, source)

    
        singleString = source{1}{row};


end