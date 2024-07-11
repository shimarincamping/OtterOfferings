import requests
import time

def find_item_name_by_id(item_id):
    store_items = requests.get("https://otterdb-default-rtdb.asia-southeast1.firebasedatabase.app/stores/" + store_id + "/storeItems.json").json()
    for i in store_items:
        if i["itemID"] == item_id:
            return i["itemName"]

def format_order_items(order_items):
    new_string = ""
    for item_id, quantity in order_items.items():
        new_string += "{} {}  x{}\n".format(item_id, find_item_name_by_id(item_id), quantity)
    
    return new_string

def print_order_details(order_data, order_text):
    print("""
======================
{}
======================
Order ID: {}
Ordered by: {}
Ordered at: {}
Order should be ready at: {}

Order items:
{}
          
Order total: {}
Order status: {}
=======================
          
          """
          .format(order_text, 
                  order_data["orderID"],
                  order_data["userID"],
                  order_data["dateTimeOrdered"],
                  order_data["dateTimeToReceive"],
                  format_order_items(order_data["orderItems"]),
                  order_data["orderTotal"],
                  order_data["orderStatus"]))

def get_api_response():
    return requests.get("https://otterdb-default-rtdb.asia-southeast1.firebasedatabase.app/orders.json")

def get_past_orders():
    latest_response = get_api_response().json()
    for id, contents in latest_response.items():
        if contents["storeID"] == store_id:
            print_order_details(contents, "ORDER RECEIVED")


def start_tracking_orders():
    latest_response = get_api_response().json()
    processed_responses = latest_response

    while True:
        time.sleep(30)
        latest_response = get_api_response().json()
        new_orders = {k:v for k,v in latest_response.items() if k not in processed_responses.keys()}

        number_of_new_orders = len(new_orders)
        if number_of_new_orders > 0:
            print("""
YOU HAVE RECEIVED {} NEW ORDERS!
                  """.format(number_of_new_orders))
            
        for id, contents in new_orders.items():
            if contents["storeID"] == store_id:
                print_order_details(contents, "NEW ORDER PLACED")

        processed_responses = latest_response

def main():
    selected_option = input("Enter '1' for all past orders, '2' to start tracking new orders: ")

    if selected_option == "1":
        get_past_orders()
    else:
        start_tracking_orders()



store_id = input("Enter store ID: ")
while True:
    main()
