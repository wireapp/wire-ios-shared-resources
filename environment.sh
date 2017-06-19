echo "export DESTINATION='platform=iOS Simulator,name=iPhone 6,OS=10.3'" >> $HOME/.circlerc
echo "export SCHEME=\"$(xcodebuild -list | awk '/^[[:space:]]*Schemes:/{getline; print $0;}' | tr -d '[:space:]') \"" >> $HOME/.circlerc
ALL_PROJECTS=( *.xcodeproj )
echo "export PROJECT='${ALL_PROJECTS[0]}'" >> $HOME/.circlerc
