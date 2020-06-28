java -jar -Duser.language=ru -Duser.region=RU libs/train-@project.version@.jar ^
--model input/not_trained_model.xlsx ^
--trainSetFile input/trainingSetReal.xlsx ^
--trainingOutputFile output/model.xlsx ^
--trainingTestSetFile input/testSetReal.xlsx ^
--trainingTestOutputFile output/test_output.xlsx ^
--trainingEpochs 10 ^
--trainingEpochsBetweenTest 5 ^
--statisticsFile output/statistics.xlsx ^
--normalizedTrainSetFile output/normalized_training_set.xlsx
pause