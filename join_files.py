import numpy as np
import pandas as pd
import os
import glob

def get_script_files(directory=None, extension="puml"):
    if directory is None:
        directory = "."
    path = os.path.join(directory, f"*.{extension}")
    return glob.glob(path)

def load_script_files(script_files):
    script_dict = {}
    for file in script_files:
        with open(file, 'r') as f:
            script_dict[file] = f.readlines()
    return script_dict

def list_to_text(script_dict, initial_text=""):
    divider = "\n\n" + "=" * 80 + "\n\n"
    script_text = initial_text
    if initial_text != "":
        script_text += divider
    for file_name, script in script_dict.items():
        script_text += "FILE NAME: " + file_name + "\n\n"
        for line in script:
            script_text += line
        script_text += divider
    return script_text

def text_to_file(text, filename):
    with open(filename + ".txt", 'w') as f:
        f.write(text)


def scripts_to_file(directory, extension, initial_text, filename):
    script_files = get_script_files(directory, extension)
    script_list = load_script_files(script_files)
    script_text = list_to_text(script_list, initial_text)
    text_to_file(script_text, filename)

if __name__ == "__main__":
    scripts_to_file("event-market/src/main/java/eventmarket/main", "java", "Classes of the event-market project:", "project-outline/project_classes")
    scripts_to_file("event-market/src/test/java/eventmarket/main", "java", "Tests of the event-market project:", "project-outline/project_tests")
    scripts_to_file("uml", "puml", "Interaction between classes:", "project-outline/uml_relations")
    scripts_to_file("uml/classes", "puml", "Classes of the project:", "project-outline/uml_classes")
    applications = [
        "event-market-creator-creation",
        "event-market-user-creation",
        "event-market-event-creation",
        "event-market-abstract",
        "event-market-ticket-purchase",
        "event-market-notification-engine",
        "event-market-financial-validation",
    ]
    for app in applications:
        scripts_to_file(f"{app}/src/main/java/camelinaction", "java", "Camel connection", "project-outline/applications-outline/" + app.replace("-", "_") + "_camel")
        scripts_to_file(f"{app}/src/main/java/eventmarketinput", "java", "Input file", "project-outline/applications-outline/" + app.replace("-", "_") + "_input_file")
    scripts_to_file("project-outline/applications-outline/", "txt", "All the applications main files:", "project-outline/all_applications_main_files")

