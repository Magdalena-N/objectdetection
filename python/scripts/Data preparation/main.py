from os import dup
from statistics import median
from firebase_admin import credentials
from firebase_admin import firestore
import matplotlib.pyplot as plt
import firebase_admin
from numpy.core.fromnumeric import mean
import pandas as pd
import numpy as np
import time

phone_dictionary = {
    "HUAWEI JNY-LX1": "Huawei P40 lite",
    "samsung SM-G991B": "Samsung Galaxy S21",
    "samsung SM-S921B": "Samsung Galaxy S24",
    "samsung SM-G988B": "Samsung Galaxy S20 Ultra",
    "samsung SM-S901B": "Samsung Galaxy S22",
    "OnePlus LE2123": "OnePlus 9 Pro",
    "motorola motorola razr 50 ultra": "Motorola Razr 50 Ultra",
}


def get_data():
    # Use a service account
    cred = credentials.Certificate(
        "object-detection-c7003-6501e080dd6d.json")
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
        try:
            createdAt = data["createdAt"]
        except KeyError:
            createdAt = ""

        for i in range(len(data["modelResults"])):
            model_name = data["modelResults"][i]["modelName"]
            model_results = data["modelResults"][i]["results"]
            round_no = data["modelResults"][i]["round"]
            delegate = data["modelResults"][i]["delegateName"]

            for result in model_results:
                results.append([id, createdAt, model_name, result["durationMeasured"], result["durationInterpreter"],
                                system_info["manufacturer"], system_info["model"], system_info["board"],
                                system_info["hardware"], system_info["apiLevel"], round_no, delegate])

                index = index + 1

    df = pd.DataFrame(results,
                      columns=["ID", "CreatedAt","Net Model", "Duration Measured [ns]", "Duration Interpreter [ns]", "Manufacturer",
                               "Model", "Board", "Hardware", "API Level", "Round", "Delegate"])

    return df, id_list


def get_round_data(data, rounds):
    rounds_results = []
    rounds_medians = []
    for round_no in rounds:
        round_data = data[data["Round"] == round_no]
        round_data = round_data["Duration Interpreter [ns]"].values.tolist()
        rounds_results.append(round_data)
        rounds_medians.append(int(np.median(round_data)))

    return rounds_results, rounds_medians


def summarize_results(df, models_mAP):
    measurements = list(set(df["ID"]))
    data = []

    for measurement in measurements:
        measurement_data = df[df["ID"] == measurement]
        createdAt = measurement_data["CreatedAt"].iloc[0]
        phone = f'{list(set(measurement_data["Manufacturer"]))[0]} {list(set(measurement_data["Model"]))[0]}'
        rounds = list(set(measurement_data["Round"]))
        net_models = list(set(measurement_data["Net Model"]))
        delegate = list(set(measurement_data["Delegate"]))[0]

        for key, value in phone_dictionary.items():
            phone = phone.replace(key, value)

        for model in net_models:
            model_data = measurement_data[measurement_data["Net Model"] == model]

            # Get round data
            rounds_results, rounds_medians = get_round_data(model_data, rounds)
            #
            # # # Plot data
            # fig_v1 = plt.figure(figsize=(10, 7))
            # ax_v1 = fig_v1.add_subplot(111)
            # ax_v1.set_title(f'Model: {model}, Phone: {phone}')
            # ax_v1.boxplot(rounds_results)
            # fig_v1.savefig(f'./object_detection/round_data/{phone}_{model}_{time.strftime("%Y%m%d-%H%M%S")}')

            # plt.show()

            model_median = median(rounds_medians)
            data.append([measurement, createdAt, phone, model, model_median, models_mAP[model], delegate])

    final_results = pd.DataFrame(data, columns=["ID", "CreatedAt", "Phone", "Net Model", "Model Median", "Model mAP", "Delegate"])

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

    df.to_csv(f'./object_detection/results_csv/Results_full_{time.strftime("%Y%m%d-%H%M%S")}.csv', index=False)

    models_mAP = {"MobilenetV1_300x300": 18, "MobilenetV2_192x192": 22, "MobilenetV3_320x320_large": 22.6,
                  "YoloV5s": 36.7, "MobilenetV3_320x320_small": 15.4, "YoloV5": 36.7}

    results = summarize_results(df, models_mAP)
    # results = remove_duplicates(results)

    # Change net model names and smartphones to a more user friendly names 
    results["Net Model"].replace(
        {"MobilenetV1_300x300": "MobileNetV1",
         "MobilenetV2_192x192": "MobileNetV2",
         "MobilenetV3_320x320_large": "MobileNetV3-large",
         "MobilenetV3_320x320_small": "MobileNetV3-small",
         "YoloV5": "YOLOv5s"},
        inplace=True)
    results["Phone"].replace(phone_dictionary, inplace=True)

    # print(results)
    results.to_csv(f'./object_detection/results_csv/Results_summarized_{time.strftime("%Y%m%d-%H%M%S")}.csv',
                   index=False)
