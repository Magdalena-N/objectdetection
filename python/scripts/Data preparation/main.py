from os import dup
from statistics import median
from firebase_admin import credentials
from firebase_admin import firestore
import matplotlib.pyplot as plt
import firebase_admin
from numpy.core.fromnumeric import mean
import pandas as pd
import numpy as np


def get_data():
    # Use a service account
    cred = credentials.Certificate(
        "object-detection-982b5-firebase-adminsdk-ggk3h-ed2f18503f.json")
    firebase_admin.initialize_app(cred)
    db = firestore.client()

    users_ref = db.collection(u'prod')
    docs = users_ref.stream()

    return docs


def transform_data(docs):
    id_list = []
    data_list = []
    index = 0

    # Transform data to dictionary
    for doc in docs:
        id_list.append(doc.id)
        data_list.append(doc.to_dict())

    doc_zip = zip(id_list, data_list)
    doc_dict = dict(doc_zip)

    results = []
    # Add data to DataFrame
    for id, data in doc_dict.items():
        system_info = data["systemInfo"]

        for i in range(len(data["modelResults"])):
            model_name = data["modelResults"][i]["modelName"]
            model_results = data["modelResults"][i]["results"]
            round_no = data["modelResults"][i]["round"]

            for result in model_results:
                results.append([id, model_name, result["durationMeasured"], result["durationInterpreter"], system_info["manufacturer"], system_info["model"], system_info["board"], system_info["hardware"], system_info["apiLevel"], round_no])

                index = index + 1

    df = pd.DataFrame(results, columns = ["ID", "Net Model", "Duration Measured", "Duration Interpreter", "Manufacturer", "Model", "Board", "Hardware", "API Level", "Round"])

    return df, id_list


def get_round_data(data, rounds):
    rounds_results = []  
    rounds_medians = []
    for round_no in rounds:
        round_data = data[data["Round"] == round_no]
        round_data = round_data["Duration Interpreter"].values.tolist()
        rounds_results.append(round_data)
        rounds_medians.append(int(np.median(round_data)))

    return rounds_results, rounds_medians


def summarize_results(df, models_mAP):
    measurements = list(set(df["ID"]))
    data = []
                
    for measurement in measurements:
        measurement_data = df[df["ID"] == measurement]
        phone = f'{list(set(measurement_data["Manufacturer"]))[0]} {list(set(measurement_data["Model"]))[0]}'
        rounds = list(set(measurement_data["Round"]))
        net_models = list(set(measurement_data["Net Model"]))

        for model in net_models:
            model_data = measurement_data[measurement_data["Net Model"] == model]
            
            # Get round data
            rounds_results, rounds_medians = get_round_data(model_data, rounds)

            # # Plot data
            # fig_v1 = plt.figure(figsize=(10, 7))
            # ax_v1 = fig_v1.add_subplot(111)
            # ax_v1.set_title(f'Model: {model}, Phone: {phone}')
            # ax_v1.boxplot(rounds_results)
            # fig_v1.savefig(f'{phone} aa {model}', format="jpg")

            # plt.show()

            model_median = median(rounds_medians)
            data.append([measurement, phone, model, model_median, models_mAP[model]])
       
    final_results = pd.DataFrame(data, columns = ["ID", "Phone", "Net Model", "Model Median", "Model mAP"])
    
    return final_results

def remove_duplicates(results):
    duplicated_phones = set(results[results.duplicated(["Phone", "Net Model"])]["Phone"].values)
    duplicated_ids = []

    for duplicate in duplicated_phones:
        duplicated_data = results[results["Phone"] == duplicate]
        ids = duplicated_data.index.tolist()   

        for net_model in set(duplicated_data["Net Model"]):
            net_model_data = duplicated_data[duplicated_data["Net Model"] == net_model]
            min_result_id = net_model_data.loc[net_model_data["Model Median"].idxmin()].name
            ids.remove(min_result_id)

        duplicated_ids.extend(ids)

    results.drop(df.index[duplicated_ids], inplace=True)

    return results


if __name__ == "__main__":
    
    data = get_data()
    df, id_list = transform_data(data)
    
    # df.to_csv(f'Results_full.csv', index=False)

    models_mAP = {"MobilenetV1_300x300": 18, "MobilenetV2_192x192": 22, "MobilenetV3_320x320_large": 22.6, "YoloV5": 37.2}
    
    results = summarize_results(df, models_mAP)
    results = remove_duplicates(results)

    # Change net model names and smartphones to a more user friendly names 
    results["Net Model"].replace({"MobilenetV1_300x300": "MobileNetV1", "MobilenetV2_192x192": "MobileNetV2", "MobilenetV3_320x320_large": "MobileNetV3", "YoloV5": "YOLOv5s"}, inplace=True)
    results["Phone"].replace({"samsung SM-A525F": "Samsung Galaxy A52", "samsung SM-M215F" : "Samsung Galaxy M21", "samsung SM-M515F" : "Samsung Galaxy M51", "samsung SM-G991B": "Samsung Galaxy S21", "samsung SM-G975F": "Samsung Galaxy S10+" , "samsung SM-G980F": "Samsung Galaxy S20", "samsung SM-A505FN": "Samsung Galaxy A50", "realme RMX2001": "Realme 6 RMX2001", "HUAWEI ANE-LX1": "Huawei P20 lite", "Xiaomi M2102J20SG": "POCO X3 Pro", "Xiaomi M2007J17G": "Xiaomi Mi 10T Lite", "Xiaomi M2011K2G" : "Xiaomi Mi 11", "LGE LG-H870" : "LG G6 H870", "OnePlus ONEPLUS A6000": "OnePlus 6 A6000"}, inplace=True)

    # print(results)
    results.to_csv(f'Results_summarized.csv', index=False)
