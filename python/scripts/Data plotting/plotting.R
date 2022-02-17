library(ggplot2)
library(ggrepel)
library(scales)

# options("scipen"=0)
setwd("C:/Users/Anytram/Desktop/Projekt badawczy/Data plotting/")
csv_data = read.csv("Results_summarized.csv")

# Function for plotting the result for a smartphone 
plot_measurement <- function(phone_data, phone) {
  ggplot() +
    geom_point(data=phone_data, aes(x=Model.Median, y=Model.mAP, color=Net.Model), size=2) +
    scale_fill_discrete(na.value=NA, guide="none") +
    geom_text() + scale_color_manual(values=c("red", "yellow" , "#000000", "blue", "#80FFFF")) +
    xlab("Inference latency [ns]") + ylab("mAP") + ggtitle(phone) + labs(color='Net Model') +
    geom_text_repel(aes(x=phone_data$Model.Median, y=phone_data$Model.mAP, label = phone_data$Net.Model),box.padding = 0.35, point.padding = 0.5,segment.color = 'grey50', size = 3) +
    theme_bw() + theme(plot.title = element_text(size=18), axis.title = element_text(size=11), axis.text = element_text(size=8))
}

# Function for plotting the result for a net model 
plot_net_model <- function(net_model_data, net_model) {
  ggplot(net_model_data, aes(reorder(Phone, -Model.Median), Model.Median)) + 
    geom_col(fill = "steelblue", width=0.7) + theme_minimal() + coord_flip() +
    geom_text(aes(label=Model.Median), hjust=-0.2, size=2.6, color='grey50') + ggtitle(net_model) +
    theme(plot.title = element_text(hjust=0.5, size=18), axis.title = element_text(size=11), axis.text = element_text(size=8)) +
    xlab("Phone model") + ylab("Inference latency [ns]") +
    scale_y_continuous(limits=c(1,max(net_model_data$Model.Median)*1.2),oob = rescale_none)
}


setwd("C:/Users/Anytram/Desktop/Projekt badawczy/Data plotting/plots/phones/")

# Plot the results for each smartphone measurement
measurements <- unique(csv_data$ID) 

for (measurement in measurements) {
  measurement_data <- csv_data[csv_data$ID == measurement, ]
  phone <- unique(csv_data[csv_data$ID == measurement,2])
  print(phone)
  measurement_plot <- print(plot_measurement(measurement_data, phone))
  # Save plot to the file
  ggsave(measurement_plot, file=paste0(phone,".png"), width = 14, height = 10, units = "cm")
  Sys.sleep(3)
}


setwd("C:/Users/Anytram/Desktop/Projekt badawczy/Data plotting/plots/mobile nets/")

# Plot the results for each mobile net
net_models <- unique(csv_data$Net.Model)

for (net_model in net_models) {
  net_model_data <- csv_data[csv_data$Net.Model == net_model, ]
  net_model_plot <- print(plot_net_model(net_model_data, net_model))
  # Save plot to the file
  ggsave(net_model_plot, file=paste0(net_model,".png"), width = 14, height = 10, units = "cm")
  Sys.sleep(3)
}



# Plot the measurement executed on different delegates
delegates_data = read.csv("Results_delegate.csv")

p <- ggplot(data=delegates_data, aes(x=Model.Median, y=Model.mAP)) +
  geom_point(aes(
    shape=Delegate,
    color=Net.Model,
    fill=Net.Model
  ), size=3) +
  scale_shape_manual(values=c(24,21,23)) +
  scale_color_manual(name = "Net Model", values=c("red", "yellow" , "#000000", "blue", "#80FFFF"), aesthetics = c("colour", "fill")) +
  guides(colour = guide_legend(override.aes = list(shape = 15))) + #use a shape that is not reserved for a specific purpose
  guides(shape = guide_legend(override.aes = list(fill = "black"))) + #use a fill color that is not reserved for a specific purpose
  guides(size = guide_legend(override.aes=list(shape = c(15,0) ))) +
  xlab("Inference latency [ns]") + ylab("mAP") + ggtitle(phone) +
  geom_text_repel(aes(label = Net.Model), box.padding = 0.35, point.padding = 0.5, segment.color = 'grey50', size = 3) +
  theme_bw() + theme(plot.title = element_text(size=18), axis.title = element_text(size=11), axis.text = element_text(size=8))

# Save plot to the file
ggsave(p, file=paste0("Xiaomi MI 8_delegates.png"), width = 14, height = 10, units = "cm")

