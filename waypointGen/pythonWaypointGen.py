#### waypoint generation ####

# Click on any point on the map to generate a node at that point.
# Click again on any existing node to delete it and redo the path
# without the node. Use 'r' to redo any deletions, and 'u' to undo
# any additions. Press 's' to save to a log file in the same directory
# as the code.

# By the way, this is terribly, terribly written code

from Tkinter import *
from PIL import Image, ImageTk
import sys
import csv
import datetime

root = Tk()
canvas = Canvas(root, width = root.winfo_screenwidth()*.9, height = root.winfo_screenheight()*.9)
buggyMap = None
nodes = []
nodeRadius = 6
undoQ = []
redoQ = []

def drawMap():
    global canvas
    global buggyMap
    canvas.create_image(canvas.winfo_reqwidth()/2,canvas.winfo_reqheight()/2, image=buggyMap, anchor = "center")

def mapDisplay():    
    global buggyMap
    global canvas
    # resize to fit window
    # should probably get a better map at some point?
    buggyMap  = Image.open("./CourseMapv1.png")
    scaleFactor = min(canvas.winfo_reqwidth()/float(list(buggyMap.size)[0]), 
        canvas.winfo_reqheight()/float(list(buggyMap.size)[1]))
    buggyMapSize = [int(var * scaleFactor) for var in list(buggyMap.size)]
    buggyMap = buggyMap.resize((buggyMapSize[0], buggyMapSize[1]), Image.ANTIALIAS)
    buggyMap = ImageTk.PhotoImage(buggyMap)
    drawMap()

    canvas.grid(row = 0, column = 0)
    canvas.bind("<Key>", keyPressedHandler)
    canvas.bind("<Button-1>", handleClick)

    root.mainloop()

def keyPressedHandler(event):
    if event.char == "s":
        save()
    elif event.char == "r":
        redo()
    elif event.char == "z":
        undo()

def redo():
    global nodes, undoQ, redoQ
    if len(redoQ) > 0:
        popped = redoQ.pop()
        if len(nodes) < 2:
            nodes += [popped]
            undoQ += [popped]
        else:
            bestNodes, bestDists, nodesIndices = findClosestNodesV2(popped, nodes)
            nodes.insert(max(nodesIndices[0], nodesIndices[1]), popped)
            undoQ += [popped]
    redrawAll()

def undo():
    global undoQ, redoQ, nodes
    if len(undoQ) > 0:
        undone = undoQ.pop()
        try: nodes.remove(undone)
        except: pass
        redoQ += [undone]
        redrawAll()

def save():
    currentTime = datetime.datetime.now().strftime("%Y_%m_%d_%H-%M-%S-%f")
    logTitle = "logs_" + currentTime + ".csv"
    logs = open(logTitle, "wb")
    writer = csv.writer(logs)
    for node in nodes:
        writer.writerow(list(node))
    logs.close()

# def showNodes():
#     global canvas
#     bestNodes, bestDists, nodesIndices = findClosestNodesV2(nodes[1], nodes)
#     drawCircleFromCenter(bestNodes[0], 10, canvas, fill = "red")
#     drawCircleFromCenter(bestNodes[1], 10, canvas, fill = "red")
#     drawCircleFromCenter(nodes[1], 10, canvas, fill = "green")

# def refreshLoop():
#     global nodes
#     newList = []
#     nodesCopy = nodes + []
#     for node in nodes:
#         nodesCopy.remove(node)
#         val = findClosestNodesV2(node, nodesCopy)
#         if val != False:
#             bestNodes, bestDists, nodesIndices = val
#         else: continue
#         newList.insert(max(nodesIndices[0], nodesIndices[1]), node)
    
#     nodes = newList
#     redrawAll()
#     print nodes
#     print newList

def handleClick(event):
    canvas.focus_set()
    global nodes
    global redoQ, undoQ
    node = (event.x, event.y)
    # if there isn't a point right next to it
    if not isClosePoint(nodes, node, nodeRadius):
        redoQ = []
        if len(nodes) < 2:
            nodes += [node]
            undoQ += [node]
        else:
            bestNodes, bestDists, nodesIndices = findClosestNodesV2(node, nodes)
            nodes.insert(max(nodesIndices[0], nodesIndices[1]), node)
            undoQ += [node]
    else:
        closestNode, dist = findClosestNode(node, nodes)
        if dist < (2*nodeRadius)**2:
            redoQ += [closestNode]
            nodes.remove(closestNode)
    redrawAll()

def isClosePoint(a,testPoint,radius):
    if len(a) == 0:
        return False
    for node in a:
        nodeX, nodeY, = node
        testPointX, testPointY = testPoint
        if (nodeX - testPointX)**2 + (nodeY - testPointY)**2 < 4*radius**2:
            return True
    return False

def findClosestNode(testNode, nodeList):
    if len(nodeList) == 0:
        return False
    bestNode = None
    bestDist = sys.maxint 
    testNodeX, testNodeY = testNode
    for node in nodeList:
        nodeX, nodeY = node
        currentDist = (testNodeX - nodeX)**2 + (testNodeY - nodeY)**2 
        if currentDist < bestDist:
            bestNode = node
            bestDist = currentDist
    return bestNode, bestDist

def findClosestNodesV2(testNode, nodeList):
    if len(nodeList) < 2:
        return False
    bestNodes = [None] * 2
    bestDists = [sys.maxint] * 2
    nodesIndices = [None] * 2
    testNodeX, testNodeY = testNode
    for i in xrange(len(nodeList) + 2):
        index = i%len(nodeList)
        if testNode != nodeList[index]:
            nodeX, nodeY = nodeList[index]
            currentDist = (testNodeX - nodeX)**2 + (testNodeY - nodeY)**2 
            if currentDist < bestDists[0]:
                bestNodes[1] = bestNodes[0]
                bestDists[1] = bestDists[0]
                bestNodes[0] = nodeList[index]
                bestDists[0] = currentDist
                nodesIndices[1] = nodesIndices[0]
                nodesIndices[0] = index
    return bestNodes, bestDists, nodesIndices

def redrawAll():
    global canvas
    global buggyMap
    global nodes
    canvas.delete(ALL)
    drawMap()
    for i in xrange(len(nodes)):
        color = '#%02x%02x%02x' % (255*(float(i)/len(nodes)), 0, 255)  # set your favourite rgb color
        drawCircleFromCenter(nodes[i], radius = nodeRadius, canvas = canvas, tags = str(hex(i)), fill = color)
        canvas.create_line(nodes[i], nodes[(i+1)%len(nodes)])
        canvas.create_text(nodes[i], text = str(i), anchor = "nw")

# draws a circle given a center point instead
# simply shifts the entire thing by the radius
def drawCircleFromCenter(coords, radius,canvas, fill = "yellow", width = 0, tags = ""):
    x, y = coords
    canvas.create_oval(x-radius, y-radius, x+radius, y+radius,
        fill = fill, width = 0, tags = tags)

mapDisplay()