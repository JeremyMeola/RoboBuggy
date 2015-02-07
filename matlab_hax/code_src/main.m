% code to take sensor data and map it out on a graph

    %initdraw

    directory = '/2014-11-23-07-23-37/';
    cd(directory);
    fileName = 'sensors.txt';

    rawData = getRawData(fileName);

    sizevec = size(rawData);
    rows = sizevec(1)

    for i = 1:2:rows,
        string1 = getEntryAtLine(i, rawData);
        string2 = getEntryAtLine(i + 1, rawData);
        string = string1 + string2
    end
    
    