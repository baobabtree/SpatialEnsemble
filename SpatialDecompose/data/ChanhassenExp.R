require(raster)
require(rpart)
require(randomForest)
require(adabag)
set.seed(ceiling(sqrt(4301901)))

#global parameter setting
global.dir = "Chanhassen/";
ref.file = paste(global.dir,"ref.tif",sep="");
feature.file = paste(global.dir,"features.tif",sep="");
sample.file = paste(global.dir,"input.texture.txt",sep="");

#read data
ref = raster(ref.file)
features = stack(feature.file)
data=read.table("Chanhassen/input.texture.txt",sep=",")
truth = values(ref);


#Exp 1: Compare global model, bagging, boosting, RF, and spatial ensemble
#now runs global models: DT, Bagging, Boosting, RF
#we prepare data, let Weka runs it
train.dat = data[data[,9]>0,1:9]
test.dat = cbind(data[data[,9]==0,1:8], truth[data[,9]==0]+1);
train.dat[,9] = as.factor(train.dat[,9])
test.dat[,9] = as.factor(test.dat[,9])
dt.fit = rpart(V9~., data=train.dat, method="class");
dt.pred = predict(dt.fit, newdata = test.dat, type="class");
dt.cm = as.matrix(table(test.dat[,9], dt.pred))
rf.fit = randomForest(V9~., data = train.dat, method ="class");
rf.pred = predict(rf.fit, newdata = test.dat, type = "class");
rf.cm = as.matrix(table(test.dat[,9], rf.pred))
bag.fit = bagging(V9~., data = train.dat, method = "class")
bag.pred = predict(bag.fit, newdata = test.dat, type="class")
bag.pred = bag.pred$class;
bag.cm = as.matrix(table(test.dat[,9], bag.pred))
boost.fit = boosting(V9~., data = train.dat, method="class");
boost.pred = predict(boost.fit, newdata = test.dat, type = "class");
boost.pred = boost.pred$class;
boost.cm = as.matrix(table(test.dat[,9], boost.pred))


#now runs spatial ensemble
m = 3; #num of footprints
footprint.file = paste(global.dir, "footprints.", as.character(m), ".txt", sep="");
footprints = read.table(footprint.file, sep=",")
for(i in unique(footprints[,1])){
	data.i = data[footprints[footprints[,1]==i,2]+1,];
	ref.i = ref[footprints[footprints[,1]==i,2]+1]
	train.i = data.i[data.i[,9]>0,1:9]
	test.i = cbind(data.i[data.i[,9]==0,1:8], ref.i[data.i[,9]==0])
}


#test sensitivity of number of zones m, DT base model
m.range = 3:4; #num of footprints
model="bagging"
for(m in m.range){
	footprint.file = paste(global.dir, "footprints.", as.character(m), ".txt", sep="");
	footprints = read.table(footprint.file, sep=",")
	res = matrix(0, nr=2, nc=2);
	for(i in unique(footprints[,1])){
		data.i = data[footprints[footprints[,1]==i,2]+1,];
		ref.i = ref[footprints[footprints[,1]==i,2]+1]
		train.i = data.i[data.i[,9]>0,1:9]
		test.i = cbind(data.i[,1:8], ref.i)
		
		if(length(unique(train.i[,9]))==1){
			dt.pred.i = rep(unique(train.i[,9]), length=nrow(data.i)) ;
			res.i = matrix(0,nr=2,nc=2);
			res.i[,unique(train.i[,9])] = as.numeric(table(ref.i));
		}
		else{
			train.i[,9] = as.factor(train.i[,9])
			test.i[,9] = as.factor(test.i[,9])
			pred.i = NULL;
			#decision tree
			if(model == "dt"){
				dt.i = rpart(V9~., data=train.i, method="class");
				pred.i = predict(dt.i, newdata = test.i, type="class");		
			}
			else if(model == "rf"){
				rf.i = randomForest(V9~., data = train.i, method ="class");
				pred.i = predict(rf.i, newdata = test.i, type = "class");
			}
			else if(model == "bagging"){
				bag.i = bagging(V9~., data = train.i, method = "class")
				pred.i = predict(bag.i, newdata = test.i, type="class")
				pred.i = pred.i$class;
			}
			else if(model == "boosting"){
				boost.i = boosting(V9~., data = train.i, method="class");
				pred.i = predict(boost.i, newdata = test.i, type = "class");
				pred.i = pred.i$class;
			}
			else {
				#Error!
			}
			res.i = table(ref.i, pred.i);
		}
		res = res + as.matrix(res.i);
	}
	print(res)
}

#sensitivity of Base Classifier Type: have to use Weka, run Java Code


#need to clean up Java Code
#sensitivity of number of patches in Homogeneous Patch generation: n


#sensitivity of parameter K in SpatialEnsemble


#sensitivity of alpha in SpatialEnsemble






