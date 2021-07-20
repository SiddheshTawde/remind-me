import React, { useEffect, useState, Fragment } from 'react';
import { Button, Dimensions, SafeAreaView, StatusBar, StyleSheet, Text, View } from 'react-native';
import { ProgressBar } from '@react-native-community/progress-bar-android';
import { getAllItems, deleteItem } from "react-native-sensitive-info";
import moment from 'moment';

const REMIND_ME_APP_STORAGE_KEY = "REMIND_ME_APP_STORAGE_KEY";

const getItems = () => {
  return getAllItems({
    sharedPreferencesName: REMIND_ME_APP_STORAGE_KEY
  })
};

const clearDay = async () => {
  return deleteItem('Day', {
    sharedPreferencesName: REMIND_ME_APP_STORAGE_KEY
  });
};

const clearProgress = async () => {
  return deleteItem('Progress', {
    sharedPreferencesName: REMIND_ME_APP_STORAGE_KEY
  });
};

function App() {

  const [day, setDay] = useState(moment().format('DD-MM-YYYY'));
  const [progress, setProgress] = useState("0.00");

  useEffect(() => {
    setInterval(async () => {
      const items = await getItems();

      setDay(items["Day"]);
      setProgress(items["Progress"]);
    }, 1000);
  }, []);

  async function reset() {
    await clearDay();
    await clearProgress();
  }

  return (
    <Fragment>
      <StatusBar
        backgroundColor="#000000"
        barStyle="light-content"
      />

      <SafeAreaView style={styles.root}>
        <View style={styles.container}>
          <Text style={styles.day}>Day {moment().diff(moment(day, "DD-MM-YYYY"), 'days')}</Text>
          <Text style={styles.progress}>Progress {(progress * 100).toFixed(2)}%</Text>
          <ProgressBar
            styleAttr="Horizontal"
            indeterminate={false}
            color="#FAFAFA"
            progress={parseFloat(progress)}
            style={styles['progress-bar']}
          />
        </View>
        <View style={styles.container}>
          <Button title="RESET" color="#000000" onPress={reset} />
        </View>
      </SafeAreaView>
    </Fragment>
  );
}

export default App;

const styles = StyleSheet.create({
  root: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    height: null,
    width: null,
    backgroundColor: "#000000"
  },
  container: {
    flex: 1,
    width: null,
    alignItems: "center",
    justifyContent: "center",
    flexDirection: "column"
  },
  day: {
    fontSize: 24,
    fontFamily: "Kinn-Bold",
    color: "#FAFAFA"
  },
  progress: {
    fontSize: 12,
    marginTop: 24,
    marginBottom: 12,
    fontFamily: "Kinn-Regular",
    color: "#FAFAFA"
  },
  'progress-bar': {
    width: Dimensions.get("screen").width * 0.25
  }
})