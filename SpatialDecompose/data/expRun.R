require(raster)
set.seed(ceiling(sqrt(4301901)))
ref = raster("BigStone/ref.tif")
features = stack("BigStone/features.tif")

nc = ref@ncols
loc = 1:ncell(ref)
loc.r = (loc-1)%/%nc+1
loc.c = (loc-1)%%nc + 1

input = data.frame(values(features[[1]]), values(features[[2]]), values(features[[3]]), values(features[[4]]), values(ref), loc.r, loc.c)

train=ref;
train[1:ncell(train)]=NA
N = 30
loc.raster = ref;
loc.raster[1:ncell(ref)]=loc
trainWetRep=sample(loc.raster[ref==1],N)
trainDryRep=sample(loc.raster[ref==0],N)
train[c(trainWetRep, trainDryRep)]=1
radius=21
train=buffer(train,width=radius) #note here the width is meters, b/3=no.of.cell

input[,5] = input[,5] +1;
input[is.na(values(train)), 5] = 0
write.table(input, "~/Research/CodeRepository/SpatialDecompose/data/BigStone/input.txt",sep=",", row.n=F, col.n=F)

newinput=cbind(input[,1:4],textures[,1:4],input[,5:7]);
write.table(newinput, "~/Research/CodeRepository/SpatialDecompose/data/Chanhassen/chanhassen.texture.input.txt",sep=",", row.n=F, col.n=F)

#read filtered points
data=read.table("BigStone/input.texture.txt",sep=",")

#read clusters: (pid, cid, label)
cls = read.table("BigStone/cluster.txt",sep=",")
cluster.ids=unique(cls[,2])
dict=array(0,max(cluster.ids)+1)
dict[cluster.ids+1]=sample(length(cluster.ids))
c.map=ref;
c.map[cls[,1]]=dict[cls[,2]+1]

jet.colors <-
  colorRampPalette(c("#00007F", "blue", "#007FFF", "cyan",
                     "#7FFF7F", "yellow", "#FF7F00", "red", "#7F0000"))
dev.new();plot(c.map,col=jet.colors(length(cluster.ids)))


#read footprints from last BipartiteEnsemble with fid-cid
footprints = read.table("~/Research/CodeRepository/SpatialDecompose/data/BigStone/footprints.txt", sep=",");
footprint.map = ref;
footprint.map[1:ncell(ref)]=NA
for(fid in unique(footprints[,1])){
	for(cid in footprints[footprints[,1]==fid,2]){
		footprint.map[cls[cls[,2]==cid,1] + 1] = fid;
	}
}
dev.new();plot(footprint.map, col=c("red","green"))


#read footprints from BisectSpatialEnsemble fid-pid
footprints = read.table("~/Research/CodeRepository/SpatialDecompose/data/BigStone/footprints.txt",sep=",")
footprint.map = ref;
footprint.map[1:ncell(ref)]=NA
for(fid in unique(footprints[,1])){
	footprint.map[footprints[footprints[,1]==fid,2]+1] = fid;
}
dev.new();plot(footprint.map, col=jet.colors(length(unique(footprints[,1]))))

#get per footprint prediction accuracy
#footprint1
res = matrix(0, nr=2,nc=2);
for(i in unique(footprints[,1])){
	data.i = data[footprints[footprints[,1]==i,2]+1,];
	train.i = data.i[data.i[,9]>0,1:9]
	ref.i = ref[footprints[footprints[,1]==i,2]+1]
	dt.i = rpart(V9~., data=train.i, method="class");
	dt.pred.i = predict(dt.i, newdata = data.i, type="class")
	res.i = table(ref.i, dt.pred.i);
	print(res.i)
	#res = res + res.i;
}









#plot training set locations
train.map = ref; train.map[data[,9]>0] = NA; dev.new(); plot(train.map, col=c("red","green"))

#test decision tree
require(rpart)
dt = rpart(V9~., data=data[data[,9]>0,1:9],method="class");
dt.pred = predict(dt, newdata = data[,1:9],type="class")
dt.map = ref; 
dt.map[1:ncell(ref)] = as.numeric(dt.pred)-1;
dt.map[dt.map==0&ref==1] = 2;
dt.map[dt.map==1&ref==0] = 3;
dev.new();plot(dt.map, col=c("red","green","black","blue"))