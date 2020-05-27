library(tidyverse)
library(data.table)
library(ggplot2)

Sys.setlocale("LC_CTYPE", "lithuanian")

# Set active dir
# dirname(rstudioapi::getSourceEditorContext()$path)
# setwd("YOUR DIRECTORY WITH THIS SCRIPT AND .CSV RESULT FILES")

# Read all .csv files from active dir
csv_read <- list.files("./", "*.csv", full.names = TRUE) %>% 
    map_df(~fread(.,colClasses=list(character=1:11)))

csv_read <- as.data.frame.matrix(csv_read) 

# Clean "-" chars (to zero)
csv_read$"Value before"[csv_read$"Value before" == "-"] <- 0
csv_read$"Value after"[csv_read$"Value after" == "-"] <- 0

# get which refactoring types and metrics are in the dataset
refactoring_types <- unique(csv_read$"Refactoring name")
metrics <- unique(csv_read$"Metric")

results <- as.data.frame(matrix(0,nrow = length(refactoring_types), ncol = length(metrics)))
colnames(results) <- metrics
rownames(results) <- refactoring_types

# get smaller datasets (a dataset for eachr refactoring type and metric) to pass to the wilcoxon test

i = 1
for(x in refactoring_types){
  
  BoxPlotsData <- as.data.frame(matrix(0,nrow = 0, ncol = 3))
  colnames(BoxPlotsData) <- c("Metrika","Tipas","Reiksme")
  
	for (y in metrics) {
		filtered <- csv_read[csv_read$"Refactoring name" == x & csv_read$"Metric" == y,]
		if (length(filtered$"Value before") > 0){
			res <- wilcox.test(as.numeric(filtered$"Value before"),as.numeric(filtered$"Value after", paired = TRUE, alternative = "two.sided"))
			print(res)
			results[x,y] <- round(res$p.value,3)
			i <- i + 1	
		}
		
		# generate a dataset for boxplots
		
		temp <- data.frame("Metrika" = rep(y,length(filtered$"Value before")), "Tipas"=rep("Pries", length(filtered$"Value before")), "Reiksme" = filtered$"Value before") 
		temp1 <- data.frame("Metrika" = rep(y,length(filtered$"Value after")), "Tipas"=rep("Po", length(filtered$"Value after")), "Reiksme" = filtered$"Value after")
		BoxPlotsData <- rbind(BoxPlotsData, temp1, temp)

	}
  
  # generate boxplots, grouped by metric before/after values
  BoxPlotsData$"Reiksme" <- as.numeric(BoxPlotsData$"Reiksme")
  BoxPlotsData$"Tipas" <- factor(
    BoxPlotsData$"Tipas", 
    levels = c("Pries", "Po")
  )
  grDevices::dev.new()
  plot <- ggplot(BoxPlotsData, aes(x=BoxPlotsData$"Metrika", y=BoxPlotsData$"Reiksme", fill=BoxPlotsData$"Tipas")) + 
    geom_boxplot() +
    facet_wrap(~BoxPlotsData$"Metrika", scale="free") + 
    xlab("Metrika") + 
    ylab("Reiksme") + 
    labs(title = x) +
    labs(fill = "Tipas")

  print(plot)
}
print(results)



